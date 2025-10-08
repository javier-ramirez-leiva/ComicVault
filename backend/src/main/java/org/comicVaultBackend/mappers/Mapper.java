package org.comicVaultBackend.mappers;

public interface Mapper<A,B> {

    B mapTo(Object a);

    A mapFrom(B b);

}
