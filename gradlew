#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname "$PRG"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            # use the system max
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock;
# also pass a default Mac OS X specific system property unless it already exists.
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS "-Xdock:name=$APP_NAME" "-Xdock:icon=$APP_HOME/media/gradle.icns""
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if $cygwin || $msys; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`

    # We build the pattern for arguments to be converted to Windows paths
    ROOTDIRSRAW=`find -L / -maxdepth 1 -mindepth 1 -type d 2>/dev/null`
    SEP=""
    for dir in $ROOTDIRSRAW ; do
        ROOTDIRS="$ROOTDIRS$SEP$dir"
        SEP="|"
    done
    NATIVEPAT="^($ROOTDIRS)(/.*?)?$"
    PATCONVEXCL="^(LOCKFILE|GRADLE_TARGET|GRADLE_USER_HOME|USER_HOME|GRADLE_HOME|GRADLE_OPTS|JVM_OPTS|JAVA_OPTS|APP_NAME|APP_BASE_NAME|DEFAULT_JVM_OPTS|MAX_FD|MAX_FD_LIMIT|CLASSPATH|JAVACMD|PRG|SAVED|APP_HOME|APP_NAME|APP_BASE_NAME|DEFAULT_JVM_OPTS)$"

    for arg in "$@" ; do
        if [[ $arg == -* ]] && ! expr "$arg" : "$PATCONVEXCL" > /dev/null ; then
            if expr "$arg" : "$NATIVEPAT" > /dev/null ; then
                CONVERTED_ARG=`cygpath --path --mixed "$arg"`
            else
                CONVERTED_ARG="$arg"
            fi
        else
            CONVERTED_ARG="$arg"
        fi
        JAVA_ARGS=("$JAVA_ARGS" "$CONVERTED_ARG")
    done
else
    JAVA_ARGS=("$@")
fi


# Collect all arguments for the java command, following the collecting last scheme.
# Collect all arguments for the java command, following the collecting last scheme.
set -- "$APP_HOME/gradle/wrapper/gradle-wrapper.jar"     org.gradle.wrapper.GradleWrapperMain     "${JAVA_ARGS[@]}"

# Start the wrapper.
#
# Before executing, tell RVM to not source any .rvmrc files in the project.
if [ -n "$rvm_path" ] && [ -n "$GEM_HOME" ] && [ -n "$GEM_PATH" ]; then
    if [ -x "$rvm_path/bin/rvm" ]; then
        # RVM has been sourced, let's ensure it does not load any .rvmrc files.
        # We are only disabling .rvmrc, other RVM features are still available.
        export rvm_project_rvmrc=0
    fi
fi

# Escape the arguments. This is necessary on some systems to prevent the Java command from failing.
# This is only done when not running on Cygwin or MSYS.
if ! ($cygwin || $msys); then
    declare -a JAVA_CMD_ARGS
    for (( i=0; i < $#; i++ )); do
        arg="${!i}"
        # If the argument contains a space, then quote it.
        if [[ "$arg" == *" "* ]]; then
            JAVA_CMD_ARGS[i]=""$arg""
        else
            JAVA_CMD_ARGS[i]="$arg"
        fi
    done
    set -- "${JAVA_CMD_ARGS[@]}"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "-Dorg.gradle.appname=$APP_BASE_NAME" -classpath "$CLASSPATH" "$@"
