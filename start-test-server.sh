#!/bin/bash

atlas-run -u 6.3.0 --jvmargs "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
