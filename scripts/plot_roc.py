#! /usr/bin/env python

from run import run_jobs, PATH
import pylab as pl
import sys
import os


def plot(filepath):
    x = []
    y = []
    with open(filepath, 'r') as fin:
        line = fin.readline()
        idx = 0
        while line:
            if idx == 5 or idx == 6:
                toks = line.split()
                if idx == 5:
                    y.append(float(toks[2]))
                else:
                    x.append(float(toks[2]))
            line = fin.readline()
            idx = (idx+1)%8
    pl.plot(x, y, label=os.path.basename(filepath)[:-3])


if __name__ == '__main__':
    pl.title("ROC Curves")
    pl.xlabel('False Positive Rate')
    pl.ylabel('True Positive Rate')
    pl.xlim(-0.1, 1.1)
    pl.ylim(-0.1, 1.1)
    run_jobs(True, False, tmin = -5, tmax = 5, tstep = .5)
    plot(os.path.join(PATH, "log/basic.log"))
    run_jobs(True, True, tmin = -5, tmax = 5, tstep = .5)
    plot(os.path.join(PATH, "log/minmax.log"))
    pl.legend()
    pl.show()
