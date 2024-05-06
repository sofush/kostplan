.PHONY: clean all
PLANTUML=plantuml

all: domain-model.png system-sequence-diagram.png class-diagram.png

domain-model.png: domain-model.txt
	$(PLANTUML) domain-model.txt

system-sequence-diagram.png: system-sequence-diagram.txt
	$(PLANTUML) system-sequence-diagram.txt

class-diagram.png: class-diagram.txt
	$(PLANTUML) class-diagram.txt

clean:
	rm *.png

