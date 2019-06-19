Direct Modifications to PRISM's Source Code
===========================================

Everything in this folder (and its sub-folders) are taken from prism-org folder, where original files 
of PRISM are located. We made our best to mark the changes in the following way:
- The beginning of the change is a single line comment starting with `=== DOWN ===`
- The end of the change is a single line comment starting with `=== UP ===`
- Original version is commented using `//` right below the first line (in fact every 
  continuous sequence of single-line comments that come right after `=== DOWN ===` is
  an original piece of code).