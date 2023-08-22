package com.ase.controller;

import com.ase.dto.JustHappened;
import com.ase.model.*;
import com.ase.repo.*;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/s3")
public class Sprint3Controller {

@Autowired
private EventRepo eventRepo;

    @Autowired
    private TravelRepo travelRepo;

    @Autowired
    private DiaryRepo diaryRepo;

    @Autowired
    private PostRepo postRepo;

@Autowired
private UserRepo userRepo;
@Autowired
private EventPostRepo eventPostRepo;

    @Autowired
    private TravelPostRepo travelPostRepo;

    @GetMapping("/event/home")
    public String home(Model model) {
        String loggIdUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Event> allEvents = eventRepo.findByEventAddedBy(loggIdUser);
        model.addAttribute("allEvents",allEvents==null?List.of():allEvents);
        return "sprint3/eventshome";
    }

    @PostMapping("/event/add")
    public String addEvent(String event) {
        Event event1 = new Event();
        event1.setEventName(event);
        event1.setEventAddedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        eventRepo.save(event1);
        return "redirect:/s3/event/home";
    }

    @GetMapping("/event/{eventId}")
    public String viewEvent(@PathVariable String eventId, Model model) {
        Event event = eventRepo.findById(eventId).get();
        model.addAttribute("eventName", event.getEventName());
        model.addAttribute("eventId", event.getEventId());
        model.addAttribute("eventPosts", eventPostRepo.findByEvent(eventId));
        return "sprint3/eventposts";

    }

    @PostMapping("/event/addPost/{eventId}")
    public String addPost(Model model, MultipartFile file, String message, @PathVariable String eventId) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        Event event = eventRepo.findById(eventId).get();
        EventPost eventPost = new EventPost();
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

        eventPost.setMessage(message);
        eventPost.setMedia(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        eventPost.setFileExtension(fileExtension);
        eventPost.setFileName(fileName);
        eventPost.setPostedTime(new Date());
        eventPost.setPostedBy(reqUser.get(0));
        eventPost.setEvent(event);
        eventPost.setComments(List.of());
        eventPostRepo.save(eventPost);

        model.addAttribute("eventName", event.getEventName());
        model.addAttribute("eventId", event.getEventId());
        model.addAttribute("eventPosts", eventPostRepo.findByEvent(eventId));

        return "sprint3/eventposts";
    }

    @GetMapping("/posts/{pid}/media")
    public ResponseEntity<byte[]> getPostMedia(@PathVariable String pid) {

        Optional<EventPost> post = eventPostRepo.findById(pid);
        if (post.isPresent()) {
            byte[] mediaBytes = post.get().getMedia().getData();
            String fileExtension = post.get().getFileExtension();
            MediaType contentType = null;
            if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
                contentType = MediaType.IMAGE_JPEG;
            } else if (fileExtension.equalsIgnoreCase("png")) {
                contentType = MediaType.IMAGE_PNG;
            } else if (fileExtension.equalsIgnoreCase("mp4")) {
                contentType = MediaType.valueOf("video/mp4");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            return new ResponseEntity<>(mediaBytes, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/events/addComment/{eventPostId}")
    public String addComment(@PathVariable String eventPostId, String comment,Model model) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        Optional<EventPost> post = eventPostRepo.findById(eventPostId);
        EventPost eventPost = post.get();
        Comments comments = new Comments();
        comments.setComment(comment);
        comments.setCommentBy(reqUser.get(0).getName());
        comments.setCommentTime(new Date());


        eventPost.getComments().add(comments);
        eventPostRepo.save(eventPost);
        Event event = eventPost.getEvent();

        model.addAttribute("eventName", event.getEventName());
        model.addAttribute("eventId", event.getEventId());
        model.addAttribute("eventPosts", eventPostRepo.findByEvent(event.getEventId()));

        return "sprint3/eventposts";

    }

    @GetMapping("/event/deletePost/{eventPostId}")
    public String deleteEventPost(@PathVariable String eventPostId,Model model) {
        Optional<EventPost> post = eventPostRepo.findById(eventPostId);
        EventPost eventPost = post.get();
        eventPostRepo.delete(eventPost);


        model.addAttribute("eventName", eventPost.getEvent().getEventName());
        model.addAttribute("eventId", eventPost.getEvent().getEventId());
        model.addAttribute("eventPosts", eventPostRepo.findByEvent(eventPost.getEvent().getEventId()));

        return "sprint3/eventposts";
    }


    @GetMapping("/friends_diary_events/{email}")
    public String friendsDiaryEvents(@PathVariable String email, Model model) {

        Diary diary = diaryRepo.findByEmail(email).get(0);
        List<User> reqUser = userRepo.findByEmail(email);
        List<Event> allEvents = eventRepo.findByEventAddedBy(email);
        model.addAttribute("friendEmail",email);
        model.addAttribute("frndName",reqUser.get(0).getName()+"'s diary");
        model.addAttribute("allEvents",allEvents);

        return "sprint3/friends_diary_events";
    }


    @GetMapping("/friend/event/{eventId}/{friendEmail}")
    public String viewEventPostOfFriend(@PathVariable String eventId, Model model,
                                        @PathVariable String friendEmail) {

        List<User> reqUser = userRepo.findByEmail(friendEmail);


        Event event = eventRepo.findById(eventId).get();
        model.addAttribute("eventName", event.getEventName());
        model.addAttribute("eventId", event.getEventId());
        model.addAttribute("eventPosts", eventPostRepo.findByEvent(eventId));

        model.addAttribute("frndName",reqUser.get(0).getName()+"'s diary");
        model.addAttribute("friendEmail",friendEmail);

        return "sprint3/friends_diary_event_posts";

    }

    @PostMapping("/friend/events/addComment/{eventPostId}/{friendEmail}")
    public String addCommentFriendPage(@PathVariable String eventPostId, String comment,Model model,
                                       @PathVariable String friendEmail) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        Optional<EventPost> post = eventPostRepo.findById(eventPostId);
        EventPost eventPost = post.get();
        Comments comments = new Comments();
        comments.setComment(comment);
        comments.setCommentBy(reqUser.get(0).getName());
        comments.setCommentTime(new Date());


        eventPost.getComments().add(comments);
        eventPostRepo.save(eventPost);
        Event event = eventPost.getEvent();



        model.addAttribute("eventName", event.getEventName());
        model.addAttribute("eventId", event.getEventId());
        model.addAttribute("frndName",reqUser.get(0).getName()+"'s diary");
        model.addAttribute("friendEmail",friendEmail);
        model.addAttribute("eventPosts", eventPostRepo.findByEvent(event.getEventId()));

        return "sprint3/friends_diary_event_posts";

    }


    //Travel link..................................................................

    @GetMapping("/travel/travelhome")
    public String travelHome(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Travel> allTravel = travelRepo.findByTravelAddedBy(email);
        model.addAttribute("allTravel",allTravel==null?List.of():allTravel);
        return "sprint3/travelhome";
    }


    @PostMapping("/travel/add")
    public String addTravel(String travel) {
        Travel travel1 = new Travel();
        travel1.setTravelName(travel);
        travel1.setTravelAddedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        travelRepo.save(travel1);

        return "redirect:/s3/travel/travelhome";
    }

    @GetMapping("/travel/{travelId}")
    public String viewTravel(@PathVariable String travelId, Model model) {
        Travel travel = travelRepo.findById(travelId).get();

            model.addAttribute("travelName", travel.getTravelName());
            model.addAttribute("travelId", travel.getTravelId());
            model.addAttribute("travelPosts", travelPostRepo.findByEvent(travelId));
            return "sprint3/travelposts";

    }


    @PostMapping("/travel/addTravelPost/{travelId}")
    public String addTravelPost(Model model, MultipartFile file, String message, @PathVariable String travelId) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        Travel travel = travelRepo.findById(travelId).get();
        TravelPost travelPost = new TravelPost();
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

        travelPost.setMessage(message);
        travelPost.setMedia(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        travelPost.setFileExtension(fileExtension);
        travelPost.setFileName(fileName);
        travelPost.setPostedTime(new Date());
        travelPost.setPostedBy(reqUser.get(0));
        travelPost.setTravel(travel);
        travelPost.setComments(List.of());
        travelPostRepo.save(travelPost);

        model.addAttribute("travelName", travel.getTravelName());
        model.addAttribute("travelId", travel.getTravelId());
        model.addAttribute("travelPosts", travelPostRepo.findByEvent(travelId));

        return "sprint3/travelposts";
    }

    @GetMapping("/travelPosts/{pid}/media")
    public ResponseEntity<byte[]> getTravelPostMedia(@PathVariable String pid) {

        Optional<TravelPost> travelPost = travelPostRepo.findById(pid);
        if (travelPost.isPresent()) {
            byte[] mediaBytes = travelPost.get().getMedia().getData();
            String fileExtension = travelPost.get().getFileExtension();
            MediaType contentType = null;
            if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
                contentType = MediaType.IMAGE_JPEG;
            } else if (fileExtension.equalsIgnoreCase("png")) {
                contentType = MediaType.IMAGE_PNG;
            } else if (fileExtension.equalsIgnoreCase("mp4")) {
                contentType = MediaType.valueOf("video/mp4");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            return new ResponseEntity<>(mediaBytes, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/travel/addComment/{travelPostId}")
    public String addTravelComment(@PathVariable String travelPostId, String comment,Model model) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        Optional<TravelPost> travelPost = travelPostRepo.findById(travelPostId);
        TravelPost travelPost1 = travelPost.get();
        Comments comments = new Comments();
        comments.setComment(comment);
        comments.setCommentBy(reqUser.get(0).getName());
        comments.setCommentTime(new Date());


        travelPost1.getComments().add(comments);
        travelPostRepo.save(travelPost1);
        Travel travel = travelPost1.getTravel();

        model.addAttribute("travelName", travel.getTravelName());
        model.addAttribute("travelId", travel.getTravelId());
        model.addAttribute("travelPosts", travelPostRepo.findByEvent(travel.getTravelId()));

        return "sprint3/travelposts";

    }

    @GetMapping("/travel/deletePost/{travelPostId}")
    public String deleteTravelPost(@PathVariable String travelPostId,Model model) {
        Optional<TravelPost> post = travelPostRepo.findById(travelPostId);
        TravelPost travelPost = post.get();
        travelPostRepo.delete(travelPost);


        model.addAttribute("travelName", travelPost.getTravel().getTravelName());
        model.addAttribute("travelId", travelPost.getTravel().getTravelId());
        model.addAttribute("travelPosts", travelPostRepo.findByEvent(travelPost.getTravel().getTravelId()));

        return "sprint3/travelposts";
    }





    @GetMapping("/friends_diary_travel/{email}")
    public String friendsDiaryTravel(@PathVariable String email, Model model) {

        Diary diary = diaryRepo.findByEmail(email).get(0);
        List<User> reqUser = userRepo.findByEmail(email);
        List<Travel> allTravels = travelRepo.findByTravelAddedBy(email);
        model.addAttribute("friendEmail",email);
        model.addAttribute("frndName",reqUser.get(0).getName()+"'s diary");
        model.addAttribute("allTravel",allTravels);

        return "sprint3/friends_diary_travel";
    }


    @GetMapping("/friend/travel/{travelId}/{friendEmail}")
    public String viewTravelPostOfFriend(@PathVariable String travelId, Model model,
                                        @PathVariable String friendEmail) {

        List<User> reqUser = userRepo.findByEmail(friendEmail);


        Travel travel = travelRepo.findById(travelId).get();
        model.addAttribute("travelName", travel.getTravelName());
        model.addAttribute("travelId", travel.getTravelId());
        model.addAttribute("travelPosts", travelPostRepo.findByEvent(travel.getTravelId()));

        model.addAttribute("frndName",reqUser.get(0).getName()+"'s diary");
        model.addAttribute("friendEmail",friendEmail);

        return "sprint3/friends_diary_travel_posts";

    }


    @PostMapping("/friend/travel/addComment/{travelPostId}/{friendEmail}")
    public String addCommentFriendTravelPage(@PathVariable String travelPostId, String comment,Model model,
                                       @PathVariable String friendEmail) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        Optional<TravelPost> post = travelPostRepo.findById(travelPostId);
        TravelPost travelPost = post.get();
        Comments comments = new Comments();
        comments.setComment(comment);
        comments.setCommentBy(reqUser.get(0).getName());
        comments.setCommentTime(new Date());


        travelPost.getComments().add(comments);
        travelPostRepo.save(travelPost);
        Travel travel = travelPost.getTravel();

        model.addAttribute("travelName", travel.getTravelName());
        model.addAttribute("travelId", travel.getTravelId());
        model.addAttribute("frndName",reqUser.get(0).getName()+"'s diary");
        model.addAttribute("friendEmail",friendEmail);
        model.addAttribute("travelPosts", travelPostRepo.findByEvent(travel.getTravelId()));

        return "sprint3/friends_diary_travel_posts";
    }

    @GetMapping("/justHappened")
    public String justHappened(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        List<TravelPost> travelPosts = travelPostRepo.findByUser(reqUser.get(0).getId());
        List<EventPost> eventPosts = eventPostRepo.findByUser(reqUser.get(0).getId());
        List<Post> allPosts = postRepo.findByUser(reqUser.get(0).getId());

        List<JustHappened> justHappenedList = new ArrayList<>();
        for (TravelPost tp : travelPosts){
            JustHappened justHappened = new JustHappened();
            justHappened.setPid(tp.getTid());
            justHappened.setMessage(tp.getMessage());
            justHappened.setFileName(tp.getFileName());
            justHappened.setFileExtension(tp.getFileExtension());
            justHappened.setPostedTime(tp.getPostedTime());
            justHappened.setPostType("Travel-" + tp.getTravel().getTravelName());
            justHappened.setComments(tp.getComments());
            justHappened.setMedia(tp.getMedia());
            justHappened.setSection("A");
            justHappenedList.add(justHappened);
        }
        for (EventPost ep : eventPosts){
            JustHappened justHappened = new JustHappened();
            justHappened.setPid(ep.getEid());
            justHappened.setMessage(ep.getMessage());
            justHappened.setFileName(ep.getFileName());
            justHappened.setFileExtension(ep.getFileExtension());
            justHappened.setPostedTime(ep.getPostedTime());
            justHappened.setPostType("Event-" + ep.getEvent().getEventName());
            justHappened.setComments(ep.getComments());
            justHappened.setMedia(ep.getMedia());
            justHappened.setSection("B");
            justHappenedList.add(justHappened);
        }

        for (Post p : allPosts){
            JustHappened justHappened = new JustHappened();
            justHappened.setPid(p.getPid());
            justHappened.setMessage(p.getMessage());
            justHappened.setFileName(p.getFileName());
            justHappened.setFileExtension(p.getFileExtension());
            justHappened.setPostedTime(p.getPostedTime());
            justHappened.setPostType("Friends");
            justHappened.setComments(List.of());
            justHappened.setMedia(p.getMedia());
            justHappened.setSection("C");
            justHappenedList.add(justHappened);

        }

        Collections.sort(justHappenedList, Comparator.comparing(JustHappened::getPostedTime).reversed());
        List<JustHappened> top10JustHappenedList = justHappenedList.stream().limit(10).collect(Collectors.toList());

        model.addAttribute("top10JustHappenedList", top10JustHappenedList);
        return "sprint3/justHappened";

    }


    @GetMapping("/jh/posts/{pid}/media/{section}")
    public ResponseEntity<byte[]> getPostMediaJH(@PathVariable String pid,@PathVariable String section) throws IOException {
        if(section.equals("A")){
            Optional<TravelPost> post = travelPostRepo.findById(pid);
            if (post.isPresent()) {
                byte[] mediaBytes = post.get().getMedia().getData();
                String fileExtension = post.get().getFileExtension();
                MediaType contentType = null;
                if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
                    contentType = MediaType.IMAGE_JPEG;
                } else if (fileExtension.equalsIgnoreCase("png")) {
                    contentType = MediaType.IMAGE_PNG;
                } else if (fileExtension.equalsIgnoreCase("mp4")) {
                    contentType = MediaType.valueOf("video/mp4");
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(contentType);
                return new ResponseEntity<>(mediaBytes, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        else if(section.equals("B")){
            Optional<EventPost> post = eventPostRepo.findById(pid);
            if (post.isPresent()) {
                byte[] mediaBytes = post.get().getMedia().getData();
                String fileExtension = post.get().getFileExtension();
                MediaType contentType = null;
                if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
                    contentType = MediaType.IMAGE_JPEG;
                } else if (fileExtension.equalsIgnoreCase("png")) {
                    contentType = MediaType.IMAGE_PNG;
                } else if (fileExtension.equalsIgnoreCase("mp4")) {
                    contentType = MediaType.valueOf("video/mp4");
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(contentType);
                return new ResponseEntity<>(mediaBytes, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        }
        else if(section.equals("C")) {
            Optional<Post> post = postRepo.findById(pid);
            if (post.isPresent()) {
                byte[] mediaBytes = post.get().getMedia().getData();
                String fileExtension = post.get().getFileExtension();
                MediaType contentType = null;
                if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
                    contentType = MediaType.IMAGE_JPEG;
                } else if (fileExtension.equalsIgnoreCase("png")) {
                    contentType = MediaType.IMAGE_PNG;
                } else if (fileExtension.equalsIgnoreCase("mp4")) {
                    contentType = MediaType.valueOf("video/mp4");
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(contentType);
                return new ResponseEntity<>(mediaBytes, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        return null;
    }

    @GetMapping("/justHappened/{email}")
    public String justHappenedFriend(Model model, @PathVariable String email) {

        List<User> reqUser = userRepo.findByEmail(email);

        List<TravelPost> travelPosts = travelPostRepo.findByUser(reqUser.get(0).getId());
        List<EventPost> eventPosts = eventPostRepo.findByUser(reqUser.get(0).getId());
        List<Post> allPosts = postRepo.findByUser(reqUser.get(0).getId());

        List<JustHappened> justHappenedList = new ArrayList<>();
        for (TravelPost tp : travelPosts){
            JustHappened justHappened = new JustHappened();
            justHappened.setPid(tp.getTid());
            justHappened.setMessage(tp.getMessage());
            justHappened.setFileName(tp.getFileName());
            justHappened.setFileExtension(tp.getFileExtension());
            justHappened.setPostedTime(tp.getPostedTime());
            justHappened.setPostType("Travel-" + tp.getTravel().getTravelName());
            justHappened.setComments(tp.getComments());
            justHappened.setMedia(tp.getMedia());
            justHappened.setSection("A");
            justHappenedList.add(justHappened);
        }
        for (EventPost ep : eventPosts){
            JustHappened justHappened = new JustHappened();
            justHappened.setPid(ep.getEid());
            justHappened.setMessage(ep.getMessage());
            justHappened.setFileName(ep.getFileName());
            justHappened.setFileExtension(ep.getFileExtension());
            justHappened.setPostedTime(ep.getPostedTime());
            justHappened.setPostType("Event-" + ep.getEvent().getEventName());
            justHappened.setComments(ep.getComments());
            justHappened.setMedia(ep.getMedia());
            justHappened.setSection("B");
            justHappenedList.add(justHappened);
        }

        for (Post p : allPosts){
            JustHappened justHappened = new JustHappened();
            justHappened.setPid(p.getPid());
            justHappened.setMessage(p.getMessage());
            justHappened.setFileName(p.getFileName());
            justHappened.setFileExtension(p.getFileExtension());
            justHappened.setPostedTime(p.getPostedTime());
            justHappened.setPostType("Friends");
            justHappened.setComments(List.of());
            justHappened.setMedia(p.getMedia());
            justHappened.setSection("C");
            justHappenedList.add(justHappened);

        }

        Collections.sort(justHappenedList, Comparator.comparing(JustHappened::getPostedTime).reversed());
        List<JustHappened> top10JustHappenedList = justHappenedList.stream().limit(10).collect(Collectors.toList());

        model.addAttribute("top10JustHappenedList", top10JustHappenedList);
        return "sprint3/justHappened";

    }

}
