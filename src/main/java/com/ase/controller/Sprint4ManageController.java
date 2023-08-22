package com.ase.controller;

import com.ase.dto.JustHappened;
import com.ase.dto.Radio;
import com.ase.model.*;
import com.ase.repo.*;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/manage")
public class Sprint4ManageController {

    @Autowired
    private EventRepo eventRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private BucketListRepo bucketListRepo;
    @Autowired
    private PostRepo postRepo;
    @Autowired
    private EventPostRepo eventPostRepo;
    @Autowired
    private TravelPostRepo travelPostRepo;
    @Autowired
    private TravelRepo travelRepo;

    @GetMapping("/bucketListHome/{email}")
    public String bucketListHome(@PathVariable String email, Model model) {
        model.addAttribute("radio", new Radio());
        User user = userRepo.findByEmail(email).get(0);
        model.addAttribute("bucketLists", bucketListRepo.findByUser(user.getId()));
        model.addAttribute("otherEmail", email);

        return "s4manage/BucketListHome";
    }


    @PostMapping("/addBucketList/{email}")
    public String addBucketList(Radio radio, Model model, MultipartFile file, String bucketListName,
                                String bucketListDescription,@PathVariable String email) throws IOException {

//        String loggIdUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get(0);

        BucketList bucketList = new BucketList();
        bucketList.setBucketListName(bucketListName);
        bucketList.setBucketListDescription(bucketListDescription);
        bucketList.setListedBy(user);
        bucketList.setStatus(radio.getStatus());
        bucketList.setCreatedAt(new Date());
        bucketList.setComments(List.of());

        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

        bucketList.setMedia(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        bucketList.setFileExtension(fileExtension);
        bucketList.setFileName(fileName);

        bucketListRepo.save(bucketList);

        model.addAttribute("otherEmail", email);

        return "redirect:/manage/bucketListHome/"+email;

    }

    @GetMapping("/deleteBucketList/{blId}/{email}")
    public String deleteBucketList(@PathVariable String blId,@PathVariable String email, Model model) {
        bucketListRepo.deleteById(blId);
        model.addAttribute("otherEmail", email);
        return "redirect:/manage/bucketListHome/"+email;
    }

    @PostMapping("/bucketList/addComment/{blId}/{email}")
    public String addBucketListComment(@PathVariable String blId, String comment,Model model, @PathVariable String email) {

//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        Optional<BucketList> bucketListOpt = bucketListRepo.findById(blId);
        BucketList bucketList = bucketListOpt.get();
        Comments comments = new Comments();
        comments.setComment(comment);
        comments.setCommentBy(reqUser.get(0).getName());
        comments.setCommentTime(new Date());


        bucketList.getComments().add(comments);
        bucketListRepo.save(bucketList);

        model.addAttribute("otherEmail", email);
        return "redirect:/manage/bucketListHome/"+email;

    }




    @GetMapping("/justHappened/{email}")
    public String justHappened(Model model, @PathVariable String email) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
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
        return "s4manage/justHappened";

    }

    @GetMapping("/eventsHome/{email}")
    public String home(Model model, @PathVariable String email) {
        List<Event> allEvents = eventRepo.findByEventAddedBy(email);
        model.addAttribute("allEvents",allEvents==null?List.of():allEvents);
        model.addAttribute("otherEmail", email);
        return "s4manage/EventHome";
    }

    @PostMapping("/event/add/{email}")
    public String addEvent(String event,@PathVariable String email, Model model) {
        Event event1 = new Event();
        event1.setEventName(event);
        event1.setEventAddedBy(email);
        eventRepo.save(event1);
        model.addAttribute("otherEmail", email);
        return "redirect:/manage/eventsHome/"+email;
    }

    @GetMapping("/event/{eventId}/{email}")
    public String viewEvent(@PathVariable String eventId, Model model, @PathVariable String email) {
        Event event = eventRepo.findById(eventId).get();
        model.addAttribute("eventName", event.getEventName());
        model.addAttribute("eventId", event.getEventId());
        model.addAttribute("eventPosts", eventPostRepo.findByEvent(eventId));
        model.addAttribute("otherEmail", email);
        return "s4manage/EventPosts";

    }

    @PostMapping("/event/addPost/{eventId}/{email}")
    public String addPost(Model model, MultipartFile file, String message,
                          @PathVariable String eventId,@PathVariable String email) throws IOException {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
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

        return "redirect:/manage/event/"+eventId+"/"+email;
    }

    @GetMapping("/event/deletePost/{eventPostId}/{email}")
    public String deleteEventPost(@PathVariable String eventPostId,Model model,
                                   @PathVariable String email) {
        Optional<EventPost> post = eventPostRepo.findById(eventPostId);
        EventPost eventPost = post.get();
        eventPostRepo.delete(eventPost);


        model.addAttribute("eventName", eventPost.getEvent().getEventName());
        model.addAttribute("eventId", eventPost.getEvent().getEventId());
        model.addAttribute("eventPosts", eventPostRepo.findByEvent(eventPost.getEvent().getEventId()));

        return "redirect:/manage/event/"+eventPost.getEvent().getEventId()+"/"+email;
    }


    @PostMapping("/events/addComment/{eventPostId}/{email}")
    public String addComment(@PathVariable String eventPostId, String comment,Model model,
                             @PathVariable String email) {
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

        return "redirect:/manage/event/"+eventPost.getEvent().getEventId()+"/"+email;

    }

    @GetMapping("/travel/travelhome/{email}")
    public String travelHome(Model model, @PathVariable String email) {
        List<Travel> allTravel = travelRepo.findByTravelAddedBy(email);
        model.addAttribute("allTravel",allTravel==null?List.of():allTravel);
        model.addAttribute("otherEmail", email);
        return "s4manage/TravelHome";
    }

    @PostMapping("/travel/add/{email}")
    public String addTravel(String travel,@PathVariable String email, Model model) {
        Travel travel1 = new Travel();
        travel1.setTravelName(travel);
        travel1.setTravelAddedBy(email);
        travelRepo.save(travel1);

        return "redirect:/manage/travel/travelhome/"+email;
    }

    @GetMapping("/travel/{travelId}/{email}")
    public String viewTravel(@PathVariable String travelId, Model model, @PathVariable String email) {
        Travel travel = travelRepo.findById(travelId).get();

        model.addAttribute("travelName", travel.getTravelName());
        model.addAttribute("travelId", travel.getTravelId());
        model.addAttribute("travelPosts", travelPostRepo.findByEvent(travelId));
        model.addAttribute("otherEmail", email);
        return "s4manage/TravelPosts";

    }

    @PostMapping("/travel/addTravelPost/{travelId}/{email}")
    public String addTravelPost(Model model, MultipartFile file, String message,
                                @PathVariable String travelId,@PathVariable String email) throws IOException {
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
        model.addAttribute("otherEmail", email);

        return "redirect:/manage/travel/"+travelId+"/"+email;
    }
    @GetMapping("/travel/deletePost/{travelPostId}/{email}")
    public String deleteTravelPost(@PathVariable String travelPostId,Model model,
                                     @PathVariable String email) {
        Optional<TravelPost> post = travelPostRepo.findById(travelPostId);
        TravelPost travelPost = post.get();
        travelPostRepo.delete(travelPost);


        model.addAttribute("travelName", travelPost.getTravel().getTravelName());
        model.addAttribute("travelId", travelPost.getTravel().getTravelId());
        model.addAttribute("travelPosts", travelPostRepo.findByEvent(travelPost.getTravel().getTravelId()));
        model.addAttribute("otherEmail", email);

        return "redirect:/manage/travel/"+travelPost.getTravel().getTravelId()+"/"+email;
    }

    @PostMapping("/travel/addComment/{travelPostId}/{email}")
    public String addTravelComment(@PathVariable String travelPostId, String comment
            ,Model model, @PathVariable String email) {

//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
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

        return  "redirect:/manage/travel/"+travel.getTravelId()+"/"+email;

    }
}
