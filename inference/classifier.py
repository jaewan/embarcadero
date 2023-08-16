from xgboost import XGBClassifier
import xgboost as xgb

model_url = "~/kafka_ml/train/fraud_model.bin"

class Classifier:
    def __init__(self, modelFile:str):
        self.model = xgb.Booster()
        self.model.load_model(model_url)

    def predict(self, recordID:str, features):
