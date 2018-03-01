#!/bin/bash

atlas-run -u 6.3.14 --jvmargs "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
