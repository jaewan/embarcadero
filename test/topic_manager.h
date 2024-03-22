#ifndef INCLUDE_TOPIC_MANGER_H_
#define INCLUDE_TOPIC_MANGER_H_

#include "cxl_manager.h"

#include <bits/stdc++.h>
#include <queue>

#define SKIP_SIZE 4
#define MAX_TOPIC_SIZE 4
#define SEGMENT_SIZE (1UL<<30)

namespace Embarcadero{

class CXLManager;

using GetNewSegmentCallback = std::function<void*()>;

class Topic{
	public:
		Topic(void* TInode_addr, const char* topic_name, int broker_id);

		// Delete copy contstructor and copy assignment operator
		Topic(const Topic &) = delete;
		Topic& operator=(const Topic &) = delete;

		void PublishToCXL(void* message, size_t size);

	private:
		//const GetNewSegmentCallback get_new_segment_callback_;
		const char* topic_name_;
		int broker_id_;
		struct TInode *tinode_;
		
		//TInode cache
		int logical_offset_;
		int written_logical_offset_;
		size_t remaining_size_;
		void* log_addr_;
		std::set<int> writing_offsets_;
		std::priority_queue<int, std::vector<int>, std::greater<int>> not_contigous_;

		//absl::mutex mu_;
		std::mutex mu_;
};

class TopicManager{
	public:
		TopicManager(CXLManager &cxl_manager, int broker_id):
									cxl_manager_(cxl_manager),
									broker_id_(broker_id){
			std::cout << "Topic Manager Initialized" << std::endl;
		}
		void CreateNewTopic(const char topic[32]);
		void DeleteTopic(char topic[32]);
		void PublishToCXL(char topic[32], void* message, size_t size);

	private:
		int GetTopicIdx(char topic[32]){
			return topic_to_idx_(topic) % MAX_TOPIC_SIZE;
		}

		CXLManager &cxl_manager_;
		static const std::hash<std::string> topic_to_idx_;
		std::map<std::string, std::unique_ptr<Topic> > topics_;
		int broker_id_;
		//absl::flat_hash_set<std::string, Topic> topics_;
};

} // End of namespace Embarcadero
#endif
