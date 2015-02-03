#!/bin/bash

# This script will queue up each anxiety for its mailing. The
# frequency of mailing is set in the schema.

psql anxietybox -c "INSERT INTO mail_to_send (anxiety_id) SELECT anxiety.id FROM anxiety JOIN account ON(anxiety.account_id = account.id) WHERE account.active=TRUE;"
