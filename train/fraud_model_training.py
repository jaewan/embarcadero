import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
from xgboost import XGBClassifier
import matplotlib
from time import perf_counter

#https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud

################## Reading Data in Batch ################## 
df_url = 'creditcard.csv'

data = pd.read_csv(df_url)

x = data.iloc[:, data.columns != 'Class']
y = data.iloc[:, data.columns == 'Class']

fraud_samples_n = len(data[data.Class==1])

fraud_ix = np.array (data[data.Class==1].index)
normal_ix = np.array (data[data.Class==0].index)

################## Data Split train/test set ################## 
undersam_normal_ix = np.random.choice (normal_ix, fraud_samples_n, replace = False )

undersam_ix = np.concatenate ([fraud_ix, undersam_normal_ix])

u_data = data.iloc[undersam_ix,:]

u_data['Class'].value_counts()

X = u_data.iloc[:,:-1]
y = u_data.iloc[:,-1]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.8, random_state=123)
print(X_train[0])
quit()

################## Model Train ################## 
start = perf_counter()
xgb = XGBClassifier(learning_rate =0.01, n_estimators=1000, objective= 'binary:logistic', max_depth=5)
xgb.fit(X_train, y_train)
end = perf_counter()
print("Model train time", end-start)

y_pred = xgb.predict(X_test)
y_pred_p = xgb.predict_proba(X_test)

print(classification_report(y_test, y_pred))

feature_important = xgb.get_booster().get_score(importance_type='weight')
keys = list(feature_important.keys())
values = list(feature_important.values())

data = pd.DataFrame(data=values, index=keys, columns=["score"]).sort_values(by = "score", ascending=False)
data.nlargest(40, columns="score").plot(kind='barh', figsize = (20,10)) ## plot top 40 features

################## Saving the Model ################## 
start = perf_counter()
xgb.save_model('fraud_model.bin')
end = perf_counter()
print("Model save time", end-start)

#X_test.to_csv("test_set.csv", header=False)
#X_test.shape
