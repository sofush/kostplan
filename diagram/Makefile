.PHONY: clean all
PLANTUML=plantuml

all: domain-model.png system-sequence-diagram.png system-sequence-diagram-1.png class-diagram.png entity-relationship.png package-diagram.png sequence-diagram.png sequence-diagram-1.png

domain-model.png: domain-model.txt
	$(PLANTUML) domain-model.txt

system-sequence-diagram.png: system-sequence-diagram.txt
	$(PLANTUML) system-sequence-diagram.txt

system-sequence-diagram-1.png: system-sequence-diagram-1.txt
	$(PLANTUML) system-sequence-diagram-1.txt

sequence-diagram.png: sequence-diagram.txt
	$(PLANTUML) sequence-diagram.txt

sequence-diagram-1.png: sequence-diagram-1.txt
	$(PLANTUML) sequence-diagram-1.txt

class-diagram.png: class-diagram.txt
	$(PLANTUML) class-diagram.txt

entity-relationship.png: entity-relationship.txt
	$(PLANTUML) entity-relationship.txt

package-diagram.png: package-diagram.txt
	$(PLANTUML) package-diagram.txt

clean:
	rm *.png

