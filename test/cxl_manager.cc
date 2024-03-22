#include "cxl_manager.h"
#include <sys/mman.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>

#include <iostream>

namespace Embarcadero{

#define CXL_SIZE (1UL << 34)
#define log_SIZE (1UL << 30)
#define NUM_CXL_IO_THREADS 2
#define MAX_TOPIC 4

CXLManager::CXLManager(int broker_id):
	broker_id_(broker_id){
	// Initialize CXL
	cxl_type_ = Emul;
	std::string cxl_path(getenv("HOME"));
	cxl_path += "/.CXL_EMUL/cxl";

	size_t cacheline_size = sysconf(_SC_LEVEL1_DCACHE_LINESIZE);

	switch(cxl_type_){
		case Emul:
			cxl_emul_fd_ = open(cxl_path.c_str(), O_RDWR, 0777);
			if (cxl_emul_fd_  < 0)
				perror("Opening Emulated CXL error");

			cxl_addr_= mmap(NULL, CXL_SIZE, PROT_READ|PROT_WRITE, MAP_SHARED, cxl_emul_fd_, 0);
			if (cxl_addr_ == MAP_FAILED)
				perror("Mapping Emulated CXL error");

			std::cout << "Successfully initialized CXL Emul" << std::endl;
			break;
		case Real:
			perror("Not implemented real cxl yet");
			break ;
	}

	// Create CXL I/O threads
	for (int i=0; i< NUM_CXL_IO_THREADS; i++)
		threads_.emplace_back(&CXLManager::CXL_io_thread, this);
	std::cout<< "Started threads:" << NUM_CXL_IO_THREADS << std::endl;

	// Initialize CXL memory regions
	size_t TINode_Region_size = sizeof(TInode) * MAX_TOPIC;
	size_t padding = TINode_Region_size - ((TINode_Region_size/cacheline_size) * cacheline_size);
	TINode_Region_size += padding;
	size_t Bitmap_Region_size = cacheline_size * MAX_TOPIC;
	size_t Segment_Region_size = (CXL_SIZE - TINode_Region_size - Bitmap_Region_size)/NUM_BROKERS;

	bitmap_ = (uint8_t*)cxl_addr_ + TINode_Region_size;
	segments_ = (uint8_t*)bitmap_ + ((broker_id_)*Segment_Region_size);

	// Wait untill al IO threads are up
	while(thread_count_.load() != NUM_CXL_IO_THREADS){}

	return;
}

CXLManager::~CXLManager(){
	//TODO(Jae) this is only for internal test. Remove this later
	while(!requestQueue_.empty()){}
	// Close CXL emulation
	switch(cxl_type_){
		case Emul:
			if (munmap(cxl_addr_, CXL_SIZE) < 0)
				perror("Unmapping Emulated CXL error");
			close(cxl_emul_fd_);

			std::cout << "Successfully deinitialized CXL Emul" << std::endl;
			break;
		case Real:
			perror("Not implemented real cxl yet");
			break;
	}

	// Stop IO threads
	{
		std::lock_guard<std::mutex> lock(queueMutex_);
		stop_threads_ = true;
		queueCondVar_.notify_all(); 
	}

	for(std::thread& thread : threads_){
		if(thread.joinable()){
			thread.join();
		}
	}
}

void CXLManager::CXL_io_thread(){
	thread_count_.fetch_add(1, std::memory_order_relaxed);
	while(!stop_threads_){
		// Sleep until a request is popped from the requestQueue
		struct publish_request req;
		{
			std::cout << " IO thread going to sleep" << std::endl;
			std::unique_lock<std::mutex> lock(queueMutex_);
			queueCondVar_.wait(lock, [this] {return !requestQueue_.empty() || stop_threads_;});
			std::cout << " IO thread woke up" << std::endl;
			if(stop_threads_)
				break;
			std::cout << requestQueue_.empty() << " from IO thread" << std::endl;
			req = requestQueue_.front();
			requestQueue_.pop();
		}

		// Actual IO to the CXL
		topic_manager_->PublishToCXL(req.topic, req.payload_address, req.size);

		// Post I/O work (as disk I/O depend on the same payload)
		int counter = req.counter->fetch_sub(1, std::memory_order_relaxed);
		if( counter == 1){
			free(req.payload_address);
		}else if(req.acknowledge){
			//TODO(Jae)
			//Enque ack request to network manager
			// network_manager_.EnqueueAckRequest();
		}
	}
}

void* CXLManager::GetTInode(const char* topic, int broker_num){
	// Convert topic to tinode address
	static const std::hash<std::string> topic_to_idx;
	int TInode_idx = topic_to_idx(topic) % MAX_TOPIC;
	return ((uint8_t*)cxl_addr_ + (TInode_idx * sizeof(struct TInode)));
}

void* CXLManager::GetNewSegment(){
	return nullptr;
}


} // End of namespace Embarcadero

int main(){
	int broker_id = 0;
	char topic[32];
	topic[0] = '0';
	Embarcadero::CXLManager cxl_manager = Embarcadero::CXLManager(broker_id);
	Embarcadero::TopicManager topic_manager = Embarcadero::TopicManager(cxl_manager, broker_id);
	cxl_manager.SetTopicManager(&topic_manager);
	topic_manager.CreateNewTopic(topic);

	int num = 1;
	Embarcadero::publish_request req[1];
	for(int i=0; i<num; i++){
		size_t size = (1UL<<20);
		req[i].topic[0] = '0';
		req[i].counter = new std::atomic<int>(1);
		req[i].payload_address = malloc(size);
		memcpy(req[i].payload_address, "Test", 4);
		req[i].size = size;
	}
	sleep(1);

	for(int i=0; i<num; i++){
		cxl_manager.EnqueueRequest(req[i]);
	}

	return 0;
}