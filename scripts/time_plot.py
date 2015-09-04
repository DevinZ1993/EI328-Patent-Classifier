#! /usr/bin/python
import pylab as pl
import string
import sys
import os

CUR_DIR = os.path.dirname(os.path.realpath(__file__))
WORKSPACE = os.path.join(CUR_DIR,"..")
DATA = os.path.join(WORKSPACE,"data")

def mean(val_list):
    total = 0.0
    for val in val_list:
        total += val
    return total/len(val_list)


if __name__=='__main__':
    x = []
    y1 = []
    y2 = []
    tmp_y1 = []
    tmp_y2 = []
    fin = open(os.path.join(DATA,"MinMax.log"),'r')
    while True:
        line = fin.readline()
        if len(line)==0:
            break
        elif line=="\t*** CHANGE N ***\n":
            if len(tmp_y1)>0:
                x.append(tmp_x)
                y1.append(mean(tmp_y1))
                y2.append(mean(tmp_y2))
                tmp_y1 = []
                tmp_y2 = []
            continue
        elif line=="\t*** CHANGE M ***\n":
            continue
        else:
            tmp_x = string.atoi(line.split()[2])
            for i in range(0,3):
                line = fin.readline()
            tmp_y1.append(string.atof(line.split()[1]))
            for i in range(0,5):
                line = fin.readline()
            tmp_y2.append(string.atof(line.split()[1]))
    p1, = pl.plot(x,y1)
    p2, = pl.plot(x,y2)
    fin.close()
    pl.title('Time vs Num of Threads')
    pl.xlabel('Num of Threads')
    pl.ylabel('Time')
    pl.legend((p1,p2),('training','testing'),'best',numpoints=1)
    pl.show()

