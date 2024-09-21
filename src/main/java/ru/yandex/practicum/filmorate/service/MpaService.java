package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDao;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaService {
    private final MpaDao mpaDao;

    public List<Mpa> getMpa() {
        return mpaDao.getMpa();
    }

    public Mpa getMpaById(int id) {
        return mpaDao.getMpaById(id);
    }
}
