package com.schambeck.marketplace.usecase.gamecontext;

import com.schambeck.marketplace.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("game-contexts")
@RequiredArgsConstructor
public class GameContextController {
    private static final GameContextMapper mapper = GameContextMapper.INSTANCE;

    private final GameContextService service;
    private final AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GameContextWeb> create(@RequestBody GameContextWeb web, Principal principal) {
        GameContext created = service.create(mapper.toEntity(web), authenticationService.getLoggedUsername(principal));
        return ResponseEntity.ok(mapper.toWeb(created));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GameContextWeb> update(@PathVariable int id, @RequestBody GameContextWeb web,
                                                  Principal principal) {
        GameContext updated = service.update(id, mapper.toEntity(web), authenticationService.getLoggedUsername(principal));
        return ResponseEntity.ok(mapper.toWeb(updated));
    }

    @GetMapping("{id}")
    public ResponseEntity<GameContextWeb> get(@PathVariable int id) {
        return ResponseEntity.ok(mapper.toWeb(service.findById(id)));
    }

    @GetMapping("{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GameContextHistory>> history(@PathVariable int id) {
        return ResponseEntity.ok(service.history(id));
    }

    @GetMapping
    public ResponseEntity<Page<GameContextWeb>> search(@PageableDefault Pageable pageable,
                                                        @RequestParam(required = false) String game,
                                                        @RequestParam(required = false) String platform,
                                                        @RequestParam(required = false) String region) {
        return ResponseEntity.ok(service.searchActive(pageable, game, platform, region).map(mapper::toWeb));
    }
}
