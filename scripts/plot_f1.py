#! /usr/bin/env python
# -*- coding: utf-8 -*-

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
            if idx == 0 or idx == 4:
                toks = line.split()
                if idx == 0:
                    x.append(int(toks[1][2:]))
                else:
                    y.append(float(toks[2]))
            line = fin.readline()
            idx = (idx+1)%8
    pl.plot(x, y)




if __name__ == '__main__':
    pl.title("F1 Scores")
    pl.xlabel("Approximate Size of Subproblems")
    pl.ylabel("F1 Score")
    run_jobs(True, True, mmin=1000, mmax=30000, mstep=1000)
    plot(os.path.join(PATH, "log/minmax.log"))
    pl.show()


