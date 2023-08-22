package com.ase.controller;

import com.ase.dto.Radio;
import com.ase.model.*;
import com.ase.repo.BucketListRepo;
import com.ase.repo.UserRepo;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/s4")
public class Sprint4Controller {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BucketListRepo bucketListRepo;

    @GetMapping("/bucketListHome")
    public String bucketListHome(Model model) {
        String loggIdUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(loggIdUser).get(0);
        model.addAttribute("radio", new Radio());
        model.addAttribute("bucketLists", bucketListRepo.findByUser(user.getId()));
        return "sprint4/bucketListHome";
    }

    @PostMapping("/addBucketList")
    public String addBucketList(Radio radio,Model model, MultipartFile file, String bucketListName,
                                String bucketListDescription) throws IOException {

        String loggIdUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(loggIdUser).get(0);

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

        model.addAttribute("bucketLists", bucketListRepo.findByUser(user.getId()));

        return "sprint4/bucketListHome";

    }

    @GetMapping("/posts/{pid}/media")
    public ResponseEntity<byte[]> getPostMedia(@PathVariable String pid) {

        Optional<BucketList> post = bucketListRepo.findById(pid);
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

    //delete bl
    @GetMapping("/deleteBucketList/{blId}")
    public String deleteBucketList(@PathVariable String blId) {
        bucketListRepo.deleteById(blId);
        return "redirect:/s4/bucketListHome";
    }

    @PostMapping("/bucketList/addComment/{blId}")
    public String addTravelComment(@PathVariable String blId, String comment,Model model) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);

        Optional<BucketList> bucketListOpt = bucketListRepo.findById(blId);
        BucketList bucketList = bucketListOpt.get();
        Comments comments = new Comments();
        comments.setComment(comment);
        comments.setCommentBy(reqUser.get(0).getName());
        comments.setCommentTime(new Date());


        bucketList.getComments().add(comments);
        bucketListRepo.save(bucketList);

        return "redirect:/s4/bucketListHome";

    }

    @GetMapping("/friend/BucketList/{email}")
    public String friendBucketList(@PathVariable String email, Model model) {
        User user = userRepo.findByEmail(email).get(0);
        model.addAttribute("friendEmail", email);
        model.addAttribute("bucketLists", bucketListRepo.findByUser(user.getId()));
        return "sprint4/friendBucketListHome";
    }

    @PostMapping("/bucketListFriend/addComment/{blId}/{email}")
    public String addTravelCommentFriend(@PathVariable String blId, String comment,Model model, @PathVariable String email) {

        String logUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(logUser);

        Optional<BucketList> bucketListOpt = bucketListRepo.findById(blId);
        BucketList bucketList = bucketListOpt.get();
        Comments comments = new Comments();
        comments.setComment(comment);
        comments.setCommentBy(reqUser.get(0).getName());
        comments.setCommentTime(new Date());


        bucketList.getComments().add(comments);
        bucketListRepo.save(bucketList);
        model.addAttribute("friendEmail", email);
        model.addAttribute("bucketLists", bucketListRepo.findByUser(userRepo.findByEmail(email).get(0).getId()));

        return "sprint4/friendBucketListHome";

    }

}
