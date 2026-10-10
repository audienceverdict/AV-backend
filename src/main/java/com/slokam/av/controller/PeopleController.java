package com.slokam.av.controller;
import com.slokam.av.dto.*;
import com.slokam.av.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/api/v1/people")
public class PeopleController {
    private final PeopleService people;
    private final MovieCreditService credits;
    public PeopleController(PeopleService people, MovieCreditService credits) { this.people = people; this.credits = credits; }
    @GetMapping public Page<PersonResponse> search(@RequestParam(required = false) String name, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return people.search(name, page, size); }
    @GetMapping("/{personId}") public PersonResponse get(@PathVariable String personId) { return people.get(personId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public PersonResponse create(@Valid @RequestBody PersonRequest r) { return people.create(r); }
    @PutMapping("/{personId}") public PersonResponse update(@PathVariable String personId, @Valid @RequestBody PersonRequest r) { return people.update(personId, r); }
    @GetMapping("/{personId}/social-links") public List<PersonSocialLinkResponse> social(@PathVariable String personId) { return people.social(personId); }
    @PostMapping("/{personId}/social-links") @ResponseStatus(HttpStatus.CREATED) public PersonSocialLinkResponse addSocial(@PathVariable String personId, @Valid @RequestBody PersonSocialLinkRequest r) { return people.addSocial(personId, r); }
    @PutMapping("/{personId}/social-links/{linkId}") public PersonSocialLinkResponse updateSocial(@PathVariable String personId, @PathVariable String linkId, @Valid @RequestBody PersonSocialLinkRequest r) { return people.updateSocial(personId, linkId, r); }
    @DeleteMapping("/{personId}/social-links/{linkId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteSocial(@PathVariable String personId, @PathVariable String linkId) { people.deleteSocial(personId, linkId); }
    @GetMapping("/{personId}/filmography") public Page<MovieCreditResponse> filmography(@PathVariable String personId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return credits.filmography(personId, page, size); }
    @GetMapping("/{personId}/movies") public Page<MovieResponse> movies(@PathVariable String personId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return credits.moviesForPerson(personId, page, size); }
}
