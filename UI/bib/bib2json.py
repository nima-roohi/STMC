#%%
import json
import re
import bibtexparser
from bibtexparser.bwriter import BibTexWriter
from bibtexparser.bibdatabase import BibDatabase
with open('C:\\Users\\yuwan\\repos\\stmc\\UI\\bib\\pubs.bib') as bibtex_file:
    bib_database = bibtexparser.load(bibtex_file)

writer = BibTexWriter()
db = BibDatabase()
papers = []
for i in range(len(bib_database.entries)):
    db.entries = [bib_database.entries[i]]
    with open('C:\\Users\\yuwan\\repos\\stmc\\UI\\bib\\' + bib_database.entries[i]['ID'], 'w') as f:
        f.write(writer.write(db))
    names = bib_database.entries[i]['author'].split(' and ')
    names = [name.split(', ') for name in names]
    authors = ''
    for name in names[:-2]:
        authors += name[1] + ' ' + name[0] + ', '
    authors += names[-2][1] + ' ' + names[-2][0] + \
        ' and ' + names[-1][1] + ' ' + names[-1][0]
    if 'journal' in bib_database.entries[i]:
        address = re.sub('}|{', '', bib_database.entries[i]['journal'] + ', vol. ' + bib_database.entries[i]['volume'] + ', no. ' + bib_database.entries[i]['number'] + ', pp. ' + bib_database.entries[i]['pages'] + ' ({})'.format(bib_database.entries[i]['year']))
    else:
        address = re.sub('}|{', '', bib_database.entries[i]['booktitle'] + ', pp. ' + bib_database.entries[i]['pages'] + ' ({})'.format(bib_database.entries[i]['year']))
    papers.append({'name': re.sub('}|{', '', bib_database.entries[i]['title']),
                   'authors': authors,
                   'address': address,
                   'bibtex': bib_database.entries[i]['ID']})

data = {'papers': papers}

with open('C:\\Users\\yuwan\\repos\\stmc\\UI\\bib\\pubs.json', 'w') as f:
    json.dump(data, f, indent=4, sort_keys=True)


#%%
