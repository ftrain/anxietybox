
FILE_ROOT=ab_responses_002 ;

FILE=/Users/ford/Dropbox/${FILE_ROOT}.csv ;

psql anxietybox -c "DROP TABLE ${FILE_ROOT};" && \
    csvsql --verbose --insert --db postgres://ford@localhost/anxietybox $FILE && \
    psql anxietybox -c "ALTER TABLE ${FILE_ROOT} ADD id SERIAL;"
;

