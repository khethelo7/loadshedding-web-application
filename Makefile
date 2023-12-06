
repackage:
	@rm lib/places-1.0-jar-with-dependencies.jar
	@rm lib/schedule-1.0-jar-with-dependencies.jar
	@rm lib/stage-1.0-jar-with-dependencies.jar
	@rm lib/web-1.0-jar-with-dependencies.jar
	@mvn clean package -DskipTests -X
	@cp web/target/web-1.0-jar-with-dependencies.jar lib/
	@cp stage/target/stage-1.0-jar-with-dependencies.jar lib/
	@cp schedule/target/schedule-1.0-jar-with-dependencies.jar lib/
	@cp places/target/places-1.0-jar-with-dependencies.jar lib/