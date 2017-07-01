#! /usr/bin/env python

import sys
import os

PATH = os.path.join(os.path.dirname(os.path.realpath(__file__)), "..")
_BIN = os.path.join(PATH, "bin")
_JAR = os.path.join(PATH, "lib/liblinear-java-1.95.jar")
_RUN = "java -classpath %s:%s com.gmail.devinz1993.minmax.jobs.Jobs" %(_BIN, _JAR)

def run_jobs(liblinear, minmax, **kargs):
    os.chdir(PATH)
    PROGRAM = _RUN
    if liblinear:
        PROGRAM += " --liblinear"
    if minmax:
        PROGRAM += " --minmax"
        if os.path.exists("log/minmax.log"):
            os.remove("log/minmax.log")
    else:
        if os.path.exists("log/basic.log"):
            os.remove("log/basic.log")
    for key in kargs:
        PROGRAM += " --%s %s" %(key, kargs[key])
    os.system(PROGRAM)


if __name__ == '__main__':
    os.chdir(PATH)
    PROGRAM = _RUN
    for arg in sys.argv[1:]:
        PROGRAM += ' '+arg
    os.system(PROGRAM)

