#! /bin/bash

set -e

CURDIR=`dirname $0`
RUN="java -classpath ${CURDIR}/bin:${CURDIR}/lib/liblinear-java-1.95.jar"

if [ $1=="0" ]; then
    make ${CURDIR}/bin/BasicTask.class
    if [ "$2"x == "0"x ]; then
        ${RUN} BasicTask --Liblinear -5 5 0.5
        python ${CURDIR}/scripts/ROC_plot.py Basic.log &
    else
        ${RUN} BasicTask --DIY 0 0 1
    fi
else
    make ${CURDIR}/bin/MinMaxTask.class
    if [ "$2"x == "0"x ]; then
        ${RUN} MinMaxTask --Liblinear -5 5 0.5 5 5 1 7000 7000 1000
        python ${CURDIR}/scripts/ROC_plot.py MinMax.log &
    else
        ${RUN} MinMaxTask --DIY 0 0 1 5 5 1 7000 7000 1000
    fi
fi


