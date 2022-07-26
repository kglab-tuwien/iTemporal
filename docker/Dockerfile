FROM registry.access.redhat.com/ubi8/openjdk-17:1.11-2
WORKDIR /benchmark
ADD temporalBenchmarkGenerator-1.0-SNAPSHOT.jar /benchmark/exec.jar
EXPOSE 8081
CMD java -Xmx12G -jar exec.jar