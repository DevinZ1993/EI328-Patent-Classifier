#! /bin/bash

set -e

CURDIR=`dirname $0`
RUN="java -classpath ${CURDIR}/bin:${CURDIR}/lib/liblinear-java-1.95.jar"

TMIN=0
TMAX=0
TSTEP=0.5
MMIN=8700
MMAX=8700
MSTEP=50
NMIN=1
NMAX=15
NSTEP=1

if [ "$1"x == "0"x ]; then
    make ${CURDIR}/bin/BasicTask.class
    if [ "$2"x == "0"x ]; then
        ${RUN} BasicTask --Liblinear ${TMIN} ${TMAX} ${TSTEP}
        if [ ${TMIN} -ne ${TMAX} ]; then
            python ${CURDIR}/scripts/ROC_plot.py Basic.log &
        fi
    else
        ${RUN} BasicTask --DIY 0 0 1
    fi
else
    make ${CURDIR}/bin/MinMaxTask.class
    if [ "$2"x == "0"x ]; then
        ${RUN} MinMaxTask --Liblinear ${TMIN} ${TMAX} ${TSTEP} ${MMIN} ${MMAX} ${MSTEP} ${NMIN} ${NMAX} ${NSTEP}
        if [ ${TMIN} -ne ${TMAX} ]; then
            python ${CURDIR}/scripts/ROC_plot.py MinMax.log &
        fi
        if [ ${NMIN} -ne ${NMAX} ]; then
            python ${CURDIR}/scripts/time_plot.py &
        fi
    else
        ${RUN} MinMaxTask --DIY 0 0 1 8700 8700 1000 7 7 1
    fi
fi


