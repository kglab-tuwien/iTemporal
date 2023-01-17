# This file allows you to run a frontend generated json from the console
# It automatically queries different provided datasizes and writes the rules for Vadalog and Meteor

import json
import argparse
import requests

parser = argparse.ArgumentParser()
parser.add_argument("--file", type=str)
parser.add_argument("--out", type=str)
parser.add_argument("--datasizes", type=str, default="10,100,1000")
args = parser.parse_args()

data = None

with open(args.file) as f:
    data = json.load(f)

data["step"] = "ALL"
data["dependencyGraph"] = json.dumps(data["graphInternal"])
del data["graphInternal"]

r = requests.post('http://localhost:8081/rules',json=data)
ruleResponse = r.json()

#print(data)
with open(args.out + "/rules.meteor", "w") as f:
    f.write(ruleResponse["rules"]["Meteor"].strip())

with open(args.out + "/rules.vada", "w") as f:
    f.write(ruleResponse["rules"]["Vadalog"].strip())


data["dependencyGraph"] = ruleResponse["graph"] #json.dumps(ruleResponse["graph"])

datasizes = args.datasizes.split(",")

for size in datasizes:
    data["properties"]["averageAmountOfGeneratedOutputs"] = size
    r = requests.post('http://localhost:8081/data',json=data)
    dataResponse = r.json()
    for (key,value) in dataResponse["data"].items():
        with open(args.out + "/data_"+key+"_"+str(size)+".csv", "w") as f:
           f.write(value.strip())
