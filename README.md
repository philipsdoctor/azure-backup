# azure-backup

Given a base directoy, uploads all the files in all sub-directories to azure storage.

Caches file paths for subsequent runs to avoid usage charges for listing files, but 
if you're not happy with your local cache just delete it and it will be rebuilt automatically.

# Example

Create a file with your connection string, call it "connection-string"

```
DefaultEndpointsProtocol=https;AccountName=your_account_name;AccountKey=your_account_key
```

# Advanced

(coming soon) You can specify a directory to exclude from the traversal

# Technical Stuff

This is a tiny program I wrote for myself, I pull down a complete list of blob files into memory, 
so if you have millions of files in your container, this may be a poor idea for you.

I make no promises about this code other than "It's working for me".
