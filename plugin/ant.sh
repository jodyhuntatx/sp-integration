#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home
export PATH="/usr/local/opt/ant@1.9/bin:$PATH"
ant "$@"
