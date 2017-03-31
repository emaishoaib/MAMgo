CREATE TABLE doc_links (
docID int(11) NOT NULL,
docLink varchar(255) NOT NULL,
PRIMARY KEY (docID)
)

*******************

CREATE TABLE tag_index (
term varchar(255) NOT NULL,
docID int(11) NOT NULL,
titleTag int(11)  DEFAULT 0,
h1Tag int(11)  DEFAULT 0,
h2Tag int(11)  DEFAULT 0,
h3Tag int(11)  DEFAULT 0,
h4Tag int(11)  DEFAULT 0,
h5Tag int(11)  DEFAULT 0,
h6Tag int(11)  DEFAULT 0,
boldTag int(11)  DEFAULT 0,
italicTag int(11)  DEFAULT 0,
otherTag int(11)  DEFAULT 0,
PRIMARY KEY (term, docID),
FOREIGN KEY (docID) REFERENCES doc_links (docID) ON DELETE CASCADE
)

*******************

CREATE TABLE pos_index (
term varchar(255) NOT NULL,
docID int(11) NOT NULL,
tagType varchar(255) NOT NULL,
tagNum int(11) NOT NULL,
posNum int(11) NOT NULL,
PRIMARY KEY (term, docID, tagType, tagNum, posNum),
FOREIGN KEY (term) REFERENCES tag_index (term) ON DELETE CASCADE
)
