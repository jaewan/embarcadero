import pandas as pd
import numpy as np
from json import dumps
from kafka import KafkaProducer
from time import perf_counter
from time import sleep
from predict_pb2 import PredictRequest
import os
import csv

# Data from
#https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud

################## Reading Data in Batch ################## 
df_url = '/home/jae/kafka_ml/train/creditcard.csv'

data = pd.read_csv(df_url)


################## Send  ################## 
kafka_addr = os.getenv('KAFKA_SERVER_ADDR')
if not kafka_addr:
    kafka_addr = 'localhost:9092'

producer = KafkaProducer(bootstrap_servers=[kafka_addr])
                         #value_serializer=lambda x:x.SerializeToString())
                         #value_serializer=lambda x:dumps(x).encode('utf-8'))

serialize_time = 0
start_time = perf_counter()
credit_tx = PredictRequest()
for j in data.iloc[0][0:-1]:
    credit_tx.featuresVector.append(float(j))
credit_tx.recordID = str(0)
credit_tx = credit_tx.SerializeToString()

for i in data.index:
    producer.send("creditcard-topic", value = credit_tx)
'''
#for i in data.index:
for i in range(1):
    ser_time = perf_counter()
    credit_tx = PredictRequest()
    for j in data.iloc[i][0:-1]:
        credit_tx.featuresVector.append(float(j))
    credit_tx.recordID = str(i)
    credit_tx = credit_tx.SerializeToString()
    serialize_time +=(perf_counter() - ser_time)
    producer.send("creditcard-topic", value = credit_tx)
'''
#for i in data.index:

end_time = perf_counter()

producer.flush()

runtime = (perf_counter() - start_time)

with open("/home/jae/kafka_ml/produce_result.csv", "a+", encoding='UTF-8', newline='') as f:
    writer = csv.writer(f)
    writer.writerow([str(runtime),str(serialize_time)])
