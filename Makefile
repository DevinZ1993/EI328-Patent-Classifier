clean:
	rm -f bin/* data/*.in data/*.out data/old_model.txt data/new_model.txt

bin/Implementor.class: src/Implementor.java
	javac -d bin src/Implementor.java

bin/LiblinAdapter.class: src/LiblinAdapter.java bin/Implementor.class
	javac -d bin -classpath bin:lib/liblinear-java-1.95.jar src/LiblinAdapter.java

bin/MyImplementor.class: src/MyImplementor.java bin/Implementor.class
	javac -d bin -classpath bin src/MyImplementor.java

bin/AbstractTask.class: src/AbstractTask.java bin/Implementor.class
	javac -d bin -classpath bin src/AbstractTask.java

bin/BasicTask.class: src/BasicTask.java bin/AbstractTask.class bin/LiblinAdapter.class bin/MyImplementor.class
	javac -d bin -classpath bin src/BasicTask.java

bin/MinMaxTask.class: src/MinMaxTask.java bin/AbstractTask.class
	javac -d bin -classpath bin src/MinMaxTask.java


