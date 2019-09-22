Direct Modifications to PRISM's Source Code
-------------------------------------------

Any java code _not_ in the `edu` folder/package is taken from PRISM's source code. We made our best to mark the changes in the following way:
- At the beginning of every change there is a single-line comment starting with `=== DOWN ===`
- At the end       of every change there is a single-line comment starting with `===  UP  ===`
- Original version (if there is any) is single-line commented and put right below the first line. 
  In fact, every continuous sequence of single-line comments that comes right after `=== DOWN ===` 
  is an original piece of code.

Last but not least, we have used a different auto-format tool, which means unmodified parts do not match line per line. But after removing white-spaces they should be _almost_ the same.  
