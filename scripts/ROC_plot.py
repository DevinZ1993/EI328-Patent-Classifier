#! /usr/bin/python

import pylab as pl
import string
import os

CUR_DIR = os.path.dirname(os.path.realpath(__file__))
WORKSPACE = os.path.join(CUR_DIR,"..")
DATA = os.path.join(WORKSPACE,"data")

if __name__=='__main__':
    fin = open(os.path.join(DATA,"roc.out"),'r')
    line = fin.readline()
    num = 0
    while len(line)>0:
        while line[0:6]!='_exit_':
            x = []
            y = []
            while line[0:6]!="_over_":
                line_list = line.split()
                x.append(string.atof(line_list[0]))
                y.append(string.atof(line_list[1]))
                line = fin.readline()
            pl.plot(x,y)
            line = fin.readline()
        pl.title('ROC Curves '+str(num))
        pl.xlabel('False Positive Rate')
        pl.ylabel('True Positive Rate')
        pl.xlim(-0.1,1.1)
        pl.ylim(0.0,1.1)
        pl.show()
        line = fin.readline()
        num += 1
    fin.close()

