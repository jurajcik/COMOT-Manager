### BEGIN INIT INFO
# Provides:          comot-manager
# Required-Start:
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start Comot Manager service daemon
# Description:       
### END INIT INFO

RUN_MODE="daemons"

JAVA=$(which java)
NAME=comot-manager
DAEMONDIR=./
DAEMON=comot-manager.jar
PIDDIR=/tmp/
PIDFILE=$PIDDIR/comot-manager.pid
LOG_DIR=/tmp/comot-manager
HTTP_PORT=8580

test -x $JAVA -a -f $DAEMONDIR/$DAEMON || echo "test -x $JAVA -a -f $DAEMONDIR/$DAEMON failed"  
test -x $JAVA -a -f $DAEMONDIR/$DAEMON || exit 0

. /lib/lsb/init-functions

case "$1" in
	start)
		log_daemon_msg "Starting Comot manager daemon"
		log_progress_msg "comot-manager"
		# Make sure we have our PIDDIR, even if it's on a tmpfs
		# install -o root -g root -m 755 -d $PIDDIR
        if ! start-stop-daemon --start --chdir $DAEMONDIR --quiet --pidfile $PIDFILE --make-pidfile --background --exec $JAVA -- -DLOG_DIR=$LOG_DIR -jar -XX:MaxPermSize=256m -Dspring.profiles.active=EMBEDDED_NEO4J_DB,INSERT_INIT_DATA $DAEMON -httpPort $HTTP_PORT; then
		    log_end_msg 1
		    exit 1
		fi
		log_end_msg 0
		;;
	stop)
		log_daemon_msg "Stopping Comot manager daemon"
		log_progress_msg "comot-manager"

		start-stop-daemon --stop --quiet --pidfile $PIDFILE
		# Wait a little and remove stale PID file
		sleep 1
		if [ -f $PIDFILE ] && ! ps h `cat $PIDFILE` > /dev/null
		then
			# Stale PID file (comot manager was succesfully stopped),
			# remove it
			rm -f $PIDFILE
		fi
		log_end_msg 0
		;;
	restart)
		$0 stop
		sleep 1
		$0 start
		;;
	status)
		pidofproc -p $PIDFILE $JAVA >/dev/null
		status=$?
		if [ $status -eq 0 ]; then
			log_success_msg "Comot manager is running"
		else
			log_failure_msg "Comot manager is not running"
		fi
		exit $status
		;;
	*)
		echo "Usage: $0 {start|stop|restart|status}"
		exit 1
		;;
esac
 
exit 0

