package vlrtstat.gg.duo.service;

import jakarta.ws.rs.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vlrtstat.gg.duo.constant.DuoMatchFilter;
import vlrtstat.gg.duo.domain.Duo;
import vlrtstat.gg.duo.dto.AddDuoDto;
import vlrtstat.gg.duo.dto.DuoDetailResponse;
import vlrtstat.gg.duo.dto.DuoDto;
import vlrtstat.gg.duo.dto.DuoListResponse;
import vlrtstat.gg.duo.error.DuoAlreadyExistError;
import vlrtstat.gg.duo.repository.DuoRepository;
import vlrtstat.gg.league.domain.LeagueEntries;
import vlrtstat.gg.league.domain.LeagueEntry;
import vlrtstat.gg.summoner.domain.Summoner;
import vlrtstat.gg.user.domain.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DuoServiceImpl implements DuoService {
    private final DuoRepository duoRepository;

    public DuoServiceImpl(DuoRepository duoRepository) {
        this.duoRepository = duoRepository;
    }

    @Override
    @Transactional
    public void addDuo(AddDuoDto addDuoDto) {
        Optional<Duo> optionalDuo = duoRepository.findLiveOne(addDuoDto.getUserId());

        if (optionalDuo.isPresent()) {
            throw new DuoAlreadyExistError();
        }

        Duo duo = new Duo();
        Summoner summoner = addDuoDto.getSummoner();
        LeagueEntries leagueEntries = addDuoDto.getLeagueEntries();
        LeagueEntry soloLeague = leagueEntries.getSoloLeague();
        duo.setUserId(addDuoDto.getUserId());
        duo.setGameName(summoner.getGameName());
        duo.setTagLine(summoner.getTagLine());
        duo.setTier(soloLeague.getTier());
        duo.setLine(addDuoDto.getLine());
        duo.setPuuid(summoner.getPuuid());
        duo.setCreatedAt(LocalDateTime.now());
        duo.setExpiredAt(LocalDateTime.now().plusHours(1));
        duo.setMemo(addDuoDto.getMemo());
        duo.setWishLines(Arrays.asList(addDuoDto.getWishLines()));
        duo.setWishTiers(Arrays.asList(addDuoDto.getWishTiers()));
        duoRepository.save(duo);
    }

    @Override
    public DuoListResponse duoList(User user, int page, DuoMatchFilter duoMatchFilter) {
        Optional<Duo> myDuo = duoRepository.findLiveOne(user.getId());
        DuoDto myDuoDto = myDuo.isEmpty() ? null : new DuoDto(myDuo.get());

        PageRequest pageRequest = PageRequest.of(page - 1, 20, Sort.Direction.DESC, "createdAt");
        Page<Duo> pageData;
        if (duoMatchFilter.equals(DuoMatchFilter.ALL)) {
            pageData = duoRepository.findAllBy(pageRequest);
        } else {
            pageData = duoRepository.findAllByIsMatched(duoMatchFilter.equals(DuoMatchFilter.MATCHED), pageRequest);
        }

        List<DuoDto> duoDtos = pageData.getContent().stream().map(duo -> new DuoDto(duo)).toList();

        return new DuoListResponse(myDuoDto, duoDtos);
    }

    @Override
    public DuoDetailResponse getDuoDetail(Long duoId) {
        Optional<Duo> optionalDuo = duoRepository.findById(duoId);
        if (optionalDuo.isEmpty()) throw new NotFoundException();
        DuoDto duoDto = new DuoDto(optionalDuo.get());
        return new DuoDetailResponse(duoDto);
    }
}