#!/bin/env perl
$email = "";
while (<STDIN>) { 
    die "TOO MANY LINES" if 1000 < $x++; 
    $email .= $_;
}
$email =~ s/'/\\'/;
$code = "215b7fa466431ae07ac879aad6ba7576";
    
open (F, "|curl -F'auth=$code&email=$email' --url http://anxietybox.com/reply");
