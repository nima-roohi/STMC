#%%
import json
import re
import bibtexparser
with open('C:\\Users\\yuwan\\repos\\stmc\\UI\\bib\\pubs.bib') as bibtex_file:
    bib_database = bibtexparser.load(bibtex_file)

papers = []
for i in range(len(bib_database.entries)):
    if 'journal' in bib_database.entries[i]:
        papers.append({'name': re.sub('}|{', '', bib_database.entries[i]['title']),
                       'authors': re.sub('}|{', '', bib_database.entries[i]['author']),
                       'address': re.sub('}|{', '', bib_database.entries[i]['journal']) + ', ' + re.sub('}|{', '', bib_database.entries[i]['pages'])})
    else:
        papers.append({'name': re.sub('}|{', '', bib_database.entries[i]['title']),
                       'authors': re.sub('}|{', '', bib_database.entries[i]['author']),
                       'address': re.sub('}|{', '', bib_database.entries[i]['booktitle']) + ', ' + re.sub('}|{', '', bib_database.entries[i]['pages'])})


data = {'papers': papers}


#%%
with open('C:\\Users\\yuwan\\repos\\stmc\\UI\\bib\\pubs.json', 'w') as f:
    json.dump(data, f, indent=4, sort_keys=True)


#%%
