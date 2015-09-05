#! /usr/bin/python
import pylab as pl
import string
import sys
import os

CUR_DIR = os.path.dirname(os.path.realpath(__file__))
WORKSPACE = os.path.join(CUR_DIR,"..")
DATA = os.path.join(WORKSPACE,"data")
BIN = os.path.join(WORKSPACE,"bin")
CLASSPATH = BIN+":"+os.path.join(WORKSPACE,"lib/liblinear-java-1.95.jar")

def plotBasic(x):
    fin = open(os.path.join(DATA,"Basic.log"),'r')
    for i in range(0,6):
        line = fin.readline()
    val = string.atof(line.split()[2])
    fin.close()
    y = [val for i in x]
    p1, = pl.plot(x,y)
    return p1

def plotMinMax(x):
    fin = open(os.path.join(DATA,"MinMax.log"),'r')
    y = []
    while True:
        line = fin.readline()
        if len(line)==0 or line=="\t*** CHANGE N ***\n":
            break
        elif line=="\t*** CHANGE M ***\n":
            continue
        else:
            for i in range(0,5):
                line = fin.readline()
            y.append(string.atof(line.split()[2]))
            for i in range(0,3):
                line = fin.readline()
    fin.close()
    p2, = pl.plot(x,y)
    return p2

if __name__=='__main__':
    mmin = 4000
    mmax = 12000
    mstep = 500
    print "run BasicTask ..."
    os.system("java -classpath "+CLASSPATH+" BasicTask --Liblinear 0 0 1")
    print "run MinMaxTask ..."
    os.system("java -classpath "+CLASSPATH+" MinMaxTask --Liblinear 0 0 1 %d %d %d 7 7 1" %(mmin,mmax,mstep))
    x = range(mmin,mmax+mstep,mstep)
    p1 = plotBasic(x)
    p2 = plotMinMax(x)
    pl.title("F1 vs Sub-Problem Size")
    pl.xlabel("Size of Sub-Problem")
    pl.ylabel('F1 Value')
    pl.legend((p1,p2),('basic','minmax'),'best',numpoints=1)
    pl.show()
    


