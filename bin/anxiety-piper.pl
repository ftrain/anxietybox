#!/bin/env perl
$email = "";
while (<STDIN>) { 
    die "TOO MANY LINES" if 1000 < $x++; 
    $email .= $_;
}
$email =~ s/'/\\'/;

open (F, "|curl -F'auth=100&email=$email' --url http://anxietybox.com/reply");
