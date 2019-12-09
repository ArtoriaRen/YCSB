if [ "$#" -ne 1 ]; then	
	echo "Usage $0 CSV_FILE"
	exit 1
fi

IFS=
ITERATION=5
DIRS=(testRes/pbft testRes/pbft_dl)
FILE_NAME=$1
DBS=(pbftdb pbftDLdb)
SERVERS=(pbft_tests/udp_server pbft_dl_tests/send_commit_list)



# header line of CSV file

for (( j=0; j<${#SERVERS[@]}; j++ ))
do
	OUTPUT_FILE=${DIRS[j]}/$FILE_NAME
	echo "read_avg, read_min, read_max, read_95, read_99, update_avg, update_min, update_max, update_95, update_99" > $OUTPUT_FILE
	for (( i=0; i<$ITERATION; i++ )) 
	do
		# start server
		$HOME/Documents/Research/sharding/bitcoin-sharding/src/test/test_bitcoin --run_test=${SERVERS[j]} &
		SERVER_PID=$!
		sleep 1

		# run test
		./bin/ycsb load ${DBS[j]} -s -P workloads/workloada
		runOut="$(./bin/ycsb run ${DBS[j]} -s -P workloads/workloada)"
		echo "runOut = "
		echo $runOut

		# collect result into CSV
		readOut="$(echo $runOut | grep "READ")"
		updateOut="$(echo $runOut | grep "UPDATE")"
		# read result
		echo $readOut | grep "Average" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		echo $readOut | grep "Min" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		echo $readOut | grep "Max" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		echo $readOut | grep "95thPercentileLatency" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		echo $readOut | grep "99thPercentileLatency" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		# write result
		echo $updateOut | grep "Average" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		echo $updateOut | grep "Min" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		echo $updateOut | grep "Max" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		echo $updateOut | grep "95thPercentileLatency" | awk '{printf $NF}' >> $OUTPUT_FILE
		echo -n "," >> $OUTPUT_FILE
		echo $updateOut | grep "99thPercentileLatency" | awk '{print $NF}' >> $OUTPUT_FILE
		# shut down server
		kill $SERVER_PID
	done
done

