package org.example.repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

/** Общая in-memory реализация CRUD, чтобы не дублировать её в пяти репозиториях. */
public abstract class InMemoryRepository<T> {
    private final ConcurrentHashMap<Long, T> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Function<T, Long> idGetter;
    private final BiConsumer<T, Long> idSetter;

    protected InMemoryRepository(Function<T, Long> idGetter, BiConsumer<T, Long> idSetter) {
        this.idGetter = idGetter;
        this.idSetter = idSetter;
    }

    /** Все записи в порядке возрастания id (у ConcurrentHashMap своего порядка нет). */
    public List<T> findAll() {
        return store.values().stream().sorted(Comparator.comparing(idGetter)).toList();
    }

    public T findById(Long id) {
        return id == null ? null : store.get(id);
    }

    public T save(T entity) {
        if (idGetter.apply(entity) == null) {
            idSetter.accept(entity, idGenerator.incrementAndGet());
        }
        store.put(idGetter.apply(entity), entity);
        return entity;
    }

    public void deleteById(Long id) {
        if (id != null) {
            store.remove(id);
        }
    }

    public boolean existsById(Long id) {
        return id != null && store.containsKey(id);
    }

    protected Stream<T> stream() {
        return findAll().stream();
    }
}
