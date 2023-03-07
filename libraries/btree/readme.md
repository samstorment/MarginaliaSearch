# BTree

This package contains a small library for creating and reading a static b-tree.

The b-trees are specified through a [BTreeContext](src/main/java/nu/marginalia/btree/model/BTreeContext.java)
which contains information about the data and index layout.

The b-trees are written through a [BTreeWriter](src/main/java/nu/marginalia/btree/BTreeWriter.java) and 
read with a [BTreeReader](src/main/java/nu/marginalia/btree/BTreeReader.java). 

## Demo

```java
BTreeContext ctx = new BTreeContext(
        4,  // num layers max
        1,  // entry size
        BTreeBlockSize.BS_4096); // page size

// Allocate a memory area to work in, see the array library for how to do this with files
LongArray array = LongArray.allocate(8192);

// Write a btree at offset 123 in the area
long[] items = new long[400];
BTreeWriter writer = new BTreeWriter(array, ctx);
final int offsetInFile = 123;

long btreeSize = writer.write(offsetInFile, items.length, slice -> {
    // here we *must* write items.length * entry.size words in slice
    // these items must be sorted!!

    for (int i = 0; i < items.length; i++) {
        slice.set(i, items[i]);
    }
});

// Read the BTree

BTreeReader reader = new BTreeReader(array, ctx, offsetInFile);
reader.findEntry(items[0]);
```