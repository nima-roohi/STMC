Direct Modifications to PRISM's Source Code
-------------------------------------------

Everything in this folder (and its sub-folders) is taken from `prism-org` folder, where original files 
of PRISM are located. We made our best to mark the changes in the following way:
- At the beginning of every change there is a single line comment starting with `=== DOWN ===`
- At the end       of every change there is a single line comment starting with `===  UP  ===`
- Original version (if there is any) is single-line commented and put right below the first line. 
  In fact, every continuous sequence of single-line comments that comes right after `=== DOWN ===` 
  is an original piece of code.

Last but not least, it is quite possible that we have used a different auto-format tool, which means
unmodified parts do not match line per line. But after removing white-spaces they should be almost 
the same.  