package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDao mpaDao;

    public List<Mpa> getMpa() {
        return mpaDao.getMpa();
    }

    public Mpa getMpaById(int id) {
        return mpaDao.getMpaById(id);
    }

    protected boolean contains(int id) {
        return mpaDao.contains(id);
    }

    protected void updateMpa(long filmId, int mpaId) {
        mpaDao.updateMpa(filmId, mpaId);
    }

    protected void deleteMpa(long filmId) {
        mpaDao.deleteMpa(filmId);
    }
}
