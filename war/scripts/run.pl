#!/usr/bin/perl
my $cmd = join(" ", @ARGV);
system("$cmd 2>\&1");
