# azure-backup

I wanted a simple utility to help me backup my local machine to Azure each night.  Given a base directoy 
and a connection string this will uploads all the files in all sub-directories to azure storage.

# Example

Create a file called settings.clj file with your connection string and local base-folder

```
{:base-folder "C:\\Users\\You\\azure-backup\\test-data"
 :connection-string "DefaultEndpointsProtocol=https;AccountName=your_account_name;AccountKey=your_account_key"}
```

Imagine your strings are Java strings in this file, and escape them accordingly.

# Advanced

(coming soon) You can specify a directory to exclude from the traversal

# Technical Stuff

This is a tiny program I wrote for myself, I pull down a complete list of blob files into memory, 
so if you have millions of files in your container, this may be a poor idea for you.

Caches file paths for subsequent runs to avoid usage charges for listing files, but 
if you're not happy with your local cache just delete it and it will be rebuilt automatically.
Other implications of the local cache mean that this cannot be run on multiple machines 
pushing content to the same container.

I make no promises about this code other than "It's working for me".
