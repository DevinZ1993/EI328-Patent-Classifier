#! /usr/bin/python
import pylab as pl
import string
import sys
import os

CUR_DIR = os.path.dirname(os.path.realpath(__file__))
WORKSPACE = os.path.join(CUR_DIR,"..")
DATA = os.path.join(WORKSPACE,"data")

if __name__=='__main__':
    x = []
    y = []
    fin = open(os.path.join(DATA,sys.argv[1]),'r')
    while True:
        line = fin.readline()
        if len(line)==0:
            break
        elif line=="\t*** CHANGE N ***\n":
            continue
        elif line=="\t*** CHANGE M ***\n":
            pl.plot(x,y)
            x = []
            y = []
        else:
            for i in range(0,6):
                line = fin.readline()
            y.append(string.atof(line.split()[2]))
            line = fin.readline()
            x.append(string.atof(line.split()[2]))
            line = fin.readline()
    fin.close()
    pl.title('ROC of '+sys.argv[1])
    pl.xlabel('False Positive Rate')
    pl.ylabel('True Positive Rate')
    pl.xlim(-0.1,1.1)
    pl.ylim(-0.1,1.1)
    pl.show()

