# This file allows you to replicate generated data to other values so that the one can test bigger datasets
# Console supp
import csv
import argparse
import requests


parser = argparse.ArgumentParser()
parser.add_argument("--file", action='append', type=str)
parser.add_argument("--out", type=str)
parser.add_argument("--replication", type=int, required=True)
args = parser.parse_args()


used_max_number = 0.0

data = {}

for file in args.file:
	data[file] = []
	
	with open(file) as f:
		csv_reader_object = csv.reader(f, delimiter=',')
		next(csv_reader_object)
		for row in csv_reader_object:
			row_new = [float(x) for x in row]
			data[file].append(row_new)
			used_max_number = max(used_max_number, max(row_new[0:-2]))

print("Max number found")
print(used_max_number)



for file in args.file:	
	with open(args.out + file,"w") as f:
		csv_writer_object = csv.writer(f, delimiter=',')
		for n in range(args.replication):
			offset = used_max_number*n
			for row in data[file]:
				row_changed = row.copy()
				for i in range(len(row_changed)-2):
					row_changed[i] = row_changed[i] + offset
				csv_writer_object.writerow(row_changed)
