#! /usr/bin/env python
# -*- coding: utf-8 -*-

from run import run_jobs, PATH
import pylab as pl
import numpy as np
import sys
import os


def plot(filepath):
    x = []
    y = []
    z = []
    with open(filepath, 'r') as fin:
        line = fin.readline()
        idx = 0
        while line:
            if idx == 0 or idx == 1 or idx == 2:
                toks = line.split()
                if idx == 0:
                    x.append(int(toks[2][2:]))
                elif idx == 1:
                    y.append(int(toks[2]))
                else:
                    z.append(int(toks[2]))
            line = fin.readline()
            idx = (idx+1)%8
    pl.plot(x, y, label="Training")
    pl.plot(x, z, label="Testing")
    pl.legend()

if __name__ == '__main__':
    pl.title('Time Performance')
    pl.xlabel('Number of Threads')
    pl.ylabel('Time')
    run_jobs(True, True, nmin=2, nmax=20, nstep=1)
    plot(os.path.join(PATH, "log/minmax.log"))
    pl.show()
 
