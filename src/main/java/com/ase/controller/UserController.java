package com.ase.controller;

import com.ase.MyFriendsDto;
import com.ase.model.*;
import com.ase.repo.*;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.model.GraphLookupOptions;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import org.bson.BsonBinarySubType;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
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

import javax.swing.event.ListDataEvent;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    DiaryRepo diaryRepo;
    @Autowired
    UserRepo userRepo;

    @Autowired
    PostRepo postRepo;

    @Autowired
    DiaryRequestRepo diaryRequestRepo;

    @Autowired
    D3JSRepo d3JSRepo;

    @Autowired
    GoogleTreeRepo googleTreeRepo;

    @GetMapping("/home")
    public String homePage( Model model) {

        model.addAttribute("user", new User());
        //check if there is an existing Directory in the database

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Diary> diary = diaryRepo.findByEmail(email);
        User user = userRepo.findByEmail(email).get(0);
        model.addAttribute("diaryExists",diary.size()>0?true:false);
        model.addAttribute("notDiaryExists",diary.size()==0?true:false);
        model.addAttribute("email",email);
        model.addAttribute("name",user.getName());

        return "user/userHome";
    }

    @GetMapping("/allDiaries")
    public String allDiaries(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get(0);

        List<Diary> diary = diaryRepo.findByEmail(email);

        List<Diary> allDiaries = diaryRepo.findAll();

        //remove frnd and family
        List<String> fam = diaryRepo.fam(user.getId()).stream().map(x->x.getEmail()).toList();
        List<String> frd = diaryRepo.frnd(user.getId()).stream().map(x->x.getEmail()).toList();



        //if already a friend or family don't show them
        if(diary!=null && !diary.isEmpty()){
            allDiaries.remove(diary.get(0));
        }

        List<String> emailsRemove = new ArrayList<>();
        emailsRemove.addAll(fam);
        emailsRemove.addAll(frd);

        List<Diary> toRemove = new ArrayList<>();
        for (Diary dairy : allDiaries) {
            if(!emailsRemove.isEmpty() && emailsRemove.contains(dairy.getEmail())){
                toRemove.add(dairy);
            }
        }

        allDiaries.removeAll(toRemove);
        model.addAttribute("allDiaries",allDiaries);
        model.addAttribute("name",user.getName());
        return "user/userexplore";

    }

    @GetMapping("/friendRequests")
    public String friendRequests(Model model) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepo.findByEmail(email).get(0);
            List<Diary> diary = diaryRepo.findByEmail(email);
        if(diary.size()>0){
            List<DiaryRequest> allRequests = diaryRequestRepo.myDairyRequests(diary.get(0).getId())
                    .stream().filter(x->x.getRequestedAs().equals("FRIEND")).toList();
            model.addAttribute("friends",allRequests);

        }
        model.addAttribute("name",user.getName());
            return "user/friendRequests";
    }


    @GetMapping("/familyRequests")
    public String familyRequests(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get(0);
        List<Diary> diary = diaryRepo.findByEmail(email);
        if(diary.size()>0){
            List<DiaryRequest> allRequests = diaryRequestRepo.myDairyRequests(diary.get(0).getId())
                    .stream().filter(x->x.getRequestedAs().equals("FAMILY")).toList();
            model.addAttribute("family",allRequests);

        }
        model.addAttribute("name",user.getName());
        return "user/familyRequests";
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get(0);
        model.addAttribute("user", user);
        return "user/userProfile";
    }


    @GetMapping("/diary")
    public String diary(){
        return "user/userDiary";
    }

    @GetMapping("/diary/create")
    public String createDiary(Model model){

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get(0);
        Diary diary = new Diary();
        diary.setEmail(email);
        diary.setName(user.getName()+"'s Diary");
        diary.setFamilyMembers(List.of());
        diary.setFriends(List.of());
        diary.setAccessFamily(List.of());
        diaryRepo.save(diary);
        model.addAttribute("diaryExists",true);

        homePage(model);
        return "user/userHome";
    }

    //DiaryHome
    @GetMapping("/diary/home")
    public String diaryHome(Model model){

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("email", email+"'s Diary");
        return "user/diaryHome";
    }


    @GetMapping("/diary/addAsFriend/{id}")
    public String addAsFriend(Model model, @PathVariable String id){

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepo.findByEmail(user).get(0);
        if(diaryRequestRepo.myDairyRequestWithUser(id, user1.getEmail()).isEmpty()){
            DiaryRequest diaryRequest = new DiaryRequest();
            diaryRequest.setDiaryId(id);
            diaryRequest.setRequestedBy(SecurityContextHolder.getContext().getAuthentication().getName());
            diaryRequest.setRequestedAs("FRIEND");
            diaryRequestRepo.save(diaryRequest);
        }
        homePage(model);

        return "redirect:/user/home";
    }
    @GetMapping("/diary/addAsFamily/{id}")
    public String addAsFamily(Model model, @PathVariable String id){

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepo.findByEmail(user).get(0);
        if(diaryRequestRepo.myDairyRequestWithUser(id, user1.getEmail()).isEmpty()){
            DiaryRequest diaryRequest = new DiaryRequest();
            diaryRequest.setDiaryId(id);
            diaryRequest.setRequestedBy(SecurityContextHolder.getContext().getAuthentication().getName());
            diaryRequest.setRequestedAs("FAMILY");
            diaryRequestRepo.save(diaryRequest);
        }

        homePage(model);

        return "user/userHome";
    }


    @GetMapping("/diary/addUser/{user}")
    public String addUser(Model model, @PathVariable String user){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Diary> diary = diaryRepo.findByEmail(email);
        List<User> reqUser = userRepo.findByEmail(user);
        List<DiaryRequest> userReq = diaryRequestRepo.myDairyRequestWithUser(diary.get(0).getId(),user);

        if(userReq.get(0).getRequestedAs().equals("FRIEND")){

            Diary diary1 = diaryRepo.findByEmail(email).get(0);
            Diary diary2 = diaryRepo.findByEmail(user).get(0);

            //need user details
            User user1 = userRepo.findByEmail(email).get(0);
            User user2 = userRepo.findByEmail(user).get(0);
            diary1.getFriends().add(user2);
            diary2.getFriends().add(user1);

            Diary saved1 = diaryRepo.save(diary1);
            diaryRepo.save(diary2);
            diaryRequestRepo.delete(userReq.get(0));
            model.addAttribute("friends",saved1.getFriends());
            return "user/myFriends";
        }
        else if(userReq.get(0).getRequestedAs().equals("FAMILY")){

            Diary diary1 = diaryRepo.findByEmail(email).get(0);
            Diary diary2 = diaryRepo.findByEmail(user).get(0);

            //need user details
            User user1 = userRepo.findByEmail(email).get(0);
            User user2 = userRepo.findByEmail(user).get(0);

            diary1.getFamilyMembers().add(user2);
            diary2.getFamilyMembers().add(user1);
            Diary saved1 = diaryRepo.save(diary1);
            diaryRepo.save(diary2);
            diaryRequestRepo.delete(userReq.get(0));
            model.addAttribute("family",saved1.getFamilyMembers());

            return "user/myFamily";
        }
        return "user/userHome";

    }



    @GetMapping("/myFriends")
    public String myFriends(Model model) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get(0);
        List<Diary> diary = diaryRepo.findByEmail(email);

        //
         List<Diary> myFriends = diaryRepo.findByFriendEmail(user.getId());

         List<MyFriendsDto> output = new ArrayList<>();
         for (Diary d : myFriends) {
             String name = d.getName();
             output.add(new MyFriendsDto(d.getEmail(),name.substring(0, name.length() - 8)));
         }


        List<User> friends = diary.get(0).getFriends();
        model.addAttribute("friends",output);
        model.addAttribute("name",user.getName());
        return "user/myFriends";

    }
    @GetMapping("/myDiary")
    public String myDairy(Model model) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> user = userRepo.findByEmail(email);
        List<Post> myPosts = postRepo.findByUser(user.get(0).getId());
        model.addAttribute("posts",myPosts);

        //add all friends
        List<Diary> diary = diaryRepo.findByEmail(email);
        List<User> friends = diary.get(0).getFriends();
        model.addAttribute("friends",friends);

        return "user/myDiary";
    }
    @GetMapping("/posts/{pid}/media")
    public ResponseEntity<byte[]> getPostMedia(@PathVariable String pid) {
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


 @GetMapping("/delete/post/{pid}")
    public String deletePost(@PathVariable String pid) {
        Optional<Post> post = postRepo.findById(pid);
     postRepo.deleteById(pid);
     return "redirect:/user/diary/friends";
    }


    @PostMapping("/addPost")
    public String addPost(Model model, MultipartFile file, String message) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);


        Post newPost = new Post();
        newPost.setMessage(message);
        newPost.setMedia(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        newPost.setFileExtension(fileExtension);
        newPost.setFileName(fileName);
        newPost.setPostedTime(new Date());
        newPost.setPostedBy(reqUser.get(0));

        myDairy(model);

        postRepo.save(newPost);
        return "redirect:/user/diary/friends";
    }


    @GetMapping("/viewFriendDiary/{email}")
    public String viewFriendDiary(Model model, @PathVariable String email) {

        User user = userRepo.findByEmail(email).get(0);
        model.addAttribute("friendEmail",email);
        model.addAttribute("frndName",user.getName()+"'s diary");
        return "user/mydiary_viewFriends_Home";
    }


    @GetMapping("/viewFamilyDiary/{email}")
    public String viewFamilyAccessDiary(Model model, @PathVariable String email) {
        String loggenInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> reqUser = userRepo.findByEmail(email);
        List<Post> myPosts = postRepo.findByUser(reqUser.get(0).getId());
        model.addAttribute("posts",myPosts);
        model.addAttribute("otherEmail",email);
        return "user/viewFriendDiary";
    }


    @GetMapping("/myFamily")
    public String myFamily(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).get(0);
        List<Diary> diary = diaryRepo.findByEmail(email);

        List<User> family = diary.get(0).getFamilyMembers();
        List<User> access = diary.get(0).getAccessFamily();
        model.addAttribute("family",family);
        model.addAttribute("access",access);

        model.addAttribute("name",user.getName());
        return "user/myFamily";
    }

    //logged in user giving access to family so family people with access can delete and add posts
    @GetMapping("/addFamilyAccess/{email}")
    public String addFamilyAccess(Model model, @PathVariable String email) {
        String loginInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Diary> diary = diaryRepo.findByEmail(loginInUser);
        User access = diary.get(0).getFamilyMembers().stream().filter(x->x.getEmail().equals(email)).toList().get(0);
        diary.get(0).getAccessFamily().add(access);
        diary.get(0).getFamilyMembers().remove(access);
        diaryRepo.save(diary.get(0));


        List<User> family = diary.get(0).getFamilyMembers();
        List<User> accessNew = diary.get(0).getAccessFamily();
        model.addAttribute("family",family);
        model.addAttribute("access",accessNew);
        return "user/myFamily";
    }

    //removeAccess
    @GetMapping("/removeAccess/{email}")
    public String removeAccess(Model model, @PathVariable String email) {
        String loginInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Diary> diary = diaryRepo.findByEmail(loginInUser);

        List<User> removeThisUser = userRepo.findByEmail(email);

        diary.get(0).getAccessFamily().remove(removeThisUser.get(0));
        diary.get(0).getFamilyMembers().add(removeThisUser.get(0));

        diaryRepo.save(diary.get(0));


        List<User> family = diary.get(0).getFamilyMembers();
        List<User> accessNew = diary.get(0).getAccessFamily();
        model.addAttribute("family",family);
        model.addAttribute("access",accessNew);
        return "user/myFamily";


    }


    //othersDiaries
    @GetMapping("/othersDiaries")
    public String othersDiaries(Model model){
        String loginInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Diary> diary = diaryRepo.findByEmail(loginInUser);

        List<User> loggedInUser = userRepo.findByEmail(loginInUser);
        List<Diary> othersDiaries = diaryRepo.access(loggedInUser.get(0).getId());
        model.addAttribute("access",othersDiaries);
        model.addAttribute("name",loggedInUser.get(0).getName());
        return "user/viewOthers";
    }


    @GetMapping("/manageHome/{email}")
    public String manageHome(Model model, @PathVariable String email) {
        model.addAttribute("otherEmail",email);
        return "user/manage_home";
    }
    @GetMapping("/manageDiary/{email}")
    public String manageDiary(Model model, @PathVariable String email) {

        List<Diary> manage = diaryRepo.findByEmail(email);
        List<User> user = userRepo.findByEmail(manage.get(0).getEmail());

        List<Post> myPosts = postRepo.findByUser(user.get(0).getId());
        model.addAttribute("posts",myPosts);
        model.addAttribute("otherEmail",email);


        return "user/manageDiary";
    }



    @PostMapping("/addOthersPost/{otherEmail}")
    public String addOthersPost(Model model, MultipartFile file, String message, @PathVariable String otherEmail) throws IOException {

        List<User> reqUser = userRepo.findByEmail(otherEmail);
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);


        Post newPost = new Post();
        newPost.setMessage(message);
        newPost.setMedia(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        newPost.setFileExtension(fileExtension);
        newPost.setFileName(fileName);
        newPost.setPostedTime(new Date());
        newPost.setPostedBy(reqUser.get(0));

        myDairy(model);

        postRepo.save(newPost);

        model =  getOthersPosts(model, otherEmail);

        return "manage/friends";
    }



    public Model getOthersPosts(Model model,String email) {

        List<Diary> manage = diaryRepo.findByEmail(email);
        List<User> user = userRepo.findByEmail(manage.get(0).getEmail());

        List<Post> myPosts = postRepo.findByUser(user.get(0).getId());
        model.addAttribute("posts",myPosts);
        model.addAttribute("otherEmail",email);

        return model;
    }



    @GetMapping("/diary/friends")
    public String friendsLink(Model model) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<User> user = userRepo.findByEmail(email);
        List<Post> myPosts = postRepo.findByUser(user.get(0).getId());
        model.addAttribute("posts",myPosts);


        //add all friends
        List<Diary> diary = diaryRepo.findByEmail(email);
        List<User> friends = diary.get(0).getFriends();
        model.addAttribute("friends",friends);


        return "user/diary_friends";
    }

    @GetMapping("/delete/post/{pid}/{email}")
    public String deletePostOther(Model model, @PathVariable String pid,@PathVariable String email) {
        Optional<Post> post = postRepo.findById(pid);
        postRepo.deleteById(pid);
        model = manageFriendsLink1(model,email);
        return "manage/friends";
    }


    public Model manageFriendsLink1(Model model, String email){

        List<Diary> manageDiary = diaryRepo.findByEmail(email);
        model.addAttribute("friends", manageDiary.get(0).getFriends());

        List<User> user = userRepo.findByEmail(manageDiary.get(0).getEmail());

        List<Post> myPosts = postRepo.findByUser(user.get(0).getId());
        model.addAttribute("posts",myPosts);
        model.addAttribute("otherEmail",email);

        return model;
    }

    @GetMapping("/manage/friends/{email}")
    public String manageFriendsLink(Model model,@PathVariable String email){

        List<Diary> manageDiary = diaryRepo.findByEmail(email);
        model.addAttribute("friends", manageDiary.get(0).getFriends());

        List<User> user = userRepo.findByEmail(manageDiary.get(0).getEmail());

        List<Post> myPosts = postRepo.findByUser(user.get(0).getId());
        model.addAttribute("posts",myPosts);
        model.addAttribute("otherEmail",email);

        return "manage/friends";
    }


    @GetMapping("/tree")
    public String tree(){

//        Tree<Integer> myTree = new Tree<Integer>();
//
        return "familyTree";
    }

    @GetMapping("/d3tree")
    public String d3tree(Model model){


/*
       D3JS d3js3 = new D3JS();
        d3js3.setName("Son");
        D3JS d3js4 = new D3JS();
        d3js4.setName("Daughter");
        d3JSRepo.save(d3js3);
        d3JSRepo.save(d3js4);


        D3JS d3js2 = new D3JS();
        d3js2.setName("Mother");
        d3js2.setChildren(List.of(d3js3,d3js4));
        d3JSRepo.save(d3js2);

        //create some children
        D3JS d3js1 = new D3JS();
        d3js1.setName("Father");
        d3JSRepo.save(d3js1);


        D3JS d3js = new D3JS();
        d3js.setName("Grand Parent");
        d3js.setChildren(List.of(d3js1,d3js2));

        Gson gson = new Gson();
        String json = gson.toJson(d3js);

        d3JSRepo.save(d3js);
        model.addAttribute("data",json);*/



        Optional<D3JS> d3js = d3JSRepo.findById("Mother");


        //java father son add
//        D3JS newNode = findByName("Mother",d3js.get());
        Gson gson = new Gson();
        String json = gson.toJson(d3js.get());
        model.addAttribute("data",json);
//        D3JS tree = d3js.get();
//        while(tree.getName().equals("Father")){
//            tree = tree.getChildren().get(0);
//
//        }






//        String name = "Mother";
//        D3JS d3js = d3JSRepo.findByName(name).get(0);
//        List<D3JS> JS = d3JSRepo.findByName(name);
//        JS.forEach(x->System.out.println(x.getName()));
//        Gson gson = new Gson();
//        String json = gson.toJson(d3js);
//        model.addAttribute("data",json);






        return "d3jstree";
    }


    public D3JS findByName(String name, D3JS root){
        if(root.getName().equals(name)){
            return root;
        }
        if(root.getChildren()!=null){
            for(D3JS child : root.getChildren()){
                D3JS result = findByName(name,child);
                if(result!=null){
                    return result;
                }
            }
        }

        return null;

    }

    @GetMapping("/chart")
    public String gt(){
        return "googleTree";
    }

    @GetMapping("/gt")
    public String showChart(Model model) {


        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<GoogleTree> ft = googleTreeRepo.findByEmail(email);


        List<Diary> diary = diaryRepo.findByEmail(email);
        User user = userRepo.findByEmail(email).get(0);



        model.addAttribute("name",user.getName());
        model.addAttribute("treeExists",ft.isPresent()?true:false);
        model.addAttribute("nottreeExists",!ft.isPresent()?true:false);


        Optional<GoogleTree> myTree = googleTreeRepo.findByEmail(email);


        if(myTree.isPresent()) {
            List<List<Object>> chartData = new ArrayList<>();
            myTree.get().getData().forEach(x -> chartData.add(Arrays.asList(x.getName(), x.getParent(), x.getTooltip())));
            model.addAttribute("data", chartData);

        }
        return "googleTree";
    }


    @GetMapping("/ft/build")
    public String buildTree(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email).get(0);
        GoogleTree googleTree = new GoogleTree();
        googleTree.setEmail(email);
        Person person = new Person(currentUser.getName(), "", "");
        googleTree.setData(List.of(person));
        googleTreeRepo.save(googleTree);

        Optional<GoogleTree> ft = googleTreeRepo.findByEmail(email);
        model.addAttribute("treeExists",ft.isPresent()?true:false);
        model.addAttribute("nottreeExists",!ft.isPresent()?true:false);

        model.addAttribute("name",currentUser.getName());

        Optional<GoogleTree> myTree = googleTreeRepo.findByEmail(email);


        if(myTree.isPresent()){
            List<List<Object>> chartData = new ArrayList<>();
            myTree.get().getData().forEach(x -> chartData.add(Arrays.asList(x.getName(),x.getParent(),x.getTooltip())));
            model.addAttribute("data", chartData);
        }
        return "googleTree";
    }


    @GetMapping("/ft/add/{name}/{addas}/{curName}")
    public String addPerson(@PathVariable String name, @PathVariable String addas, Model model,@PathVariable String curName) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<GoogleTree> ft = googleTreeRepo.findByEmail(email);
        model.addAttribute("treeExists",ft.isPresent()?true:false);
        model.addAttribute("nottreeExists",!ft.isPresent()?true:false);


        if (addas.equalsIgnoreCase("CHILD")){
            ft.get().getData().add(new Person(name,curName , ""));
            googleTreeRepo.save(ft.get());
        }
        if (addas.equalsIgnoreCase("PARENT")){

            String newUser = name;
            String currentNode = curName;
            String currentParent = ft.get().getData().stream().filter(x->x.getName().equals(currentNode)).toList().get(0).getParent();

            Person newPerson = new Person(newUser, currentParent, "");

            ft.get().getData().stream().filter(x->x.getName().equals(currentNode)).toList().get(0).setParent(newUser);
            ft.get().getData().add(newPerson);
            googleTreeRepo.save(ft.get());
        }
       return "googleTree";
    }

    @GetMapping("/manage/frndreq/{email}")
    public String manageFriendRequests(@PathVariable String email, Model model) {
        Diary diary = diaryRepo.findByEmail(email).get(0);
        List<DiaryRequest> allRequests = diaryRequestRepo.myDairyRequests(diary.getId());
        model.addAttribute("friends",allRequests);
        model.addAttribute("otherEmail",email);

        return "manage/friendRequests";
    }

    @GetMapping("/manage/famreq/{email}")
    public String manageFamilyRequests(@PathVariable String email, Model model) {
        Diary diary = diaryRepo.findByEmail(email).get(0);
        List<DiaryRequest> allRequests = diaryRequestRepo.myDairyRequests(diary.getId());
        model.addAttribute("family",allRequests);

        return "manage/familyRequests";
    }

    @GetMapping("/manage/addUser/{requestedBy}/{otherEmail}")
    public String manageAddUser(Model model, @PathVariable String requestedBy, @PathVariable String otherEmail) {

        List<DiaryRequest> allRequests = diaryRequestRepo.myDairyRequests(diaryRepo.findByEmail(otherEmail).get(0).getId());
        DiaryRequest request = allRequests.stream().filter(x->x.getRequestedBy().equals(requestedBy)).toList().get(0);
        if (request.getRequestedAs().equals("FRIEND")) {
            Diary dairy1 = diaryRepo.findByEmail(otherEmail).get(0);
            Diary diary2 = diaryRepo.findByEmail(requestedBy).get(0);

            User user1 = userRepo.findByEmail(otherEmail).get(0);
            User user2 = userRepo.findByEmail(requestedBy).get(0);

            dairy1.getFriends().add(user2);
            diary2.getFriends().add(user1);
            diaryRepo.save(dairy1);
            diaryRepo.save(diary2);
            diaryRequestRepo.delete(request);

            Diary diary = diaryRepo.findByEmail(otherEmail).get(0);
            List<DiaryRequest> otherReq = diaryRequestRepo.myDairyRequests(diary.getId());
            model.addAttribute("friends", allRequests);
            return "manage/friendRequests";
        } else if (request.getRequestedAs().equals("FAMILY")) {
            Diary diary1 = diaryRepo.findByEmail(otherEmail).get(0);
            Diary diary2 = diaryRepo.findByEmail(requestedBy).get(0);

            User user1 = userRepo.findByEmail(otherEmail).get(0);
            User user2 = userRepo.findByEmail(requestedBy).get(0);

            diary1.getFamilyMembers().add(user2);
            diary2.getFamilyMembers().add(user1);
            diaryRepo.save(diary1);
            diaryRepo.save(diary2);
            diaryRequestRepo.delete(request);

            Diary diary = diaryRepo.findByEmail(otherEmail).get(0);
            List<DiaryRequest> otherReq = diaryRequestRepo.myDairyRequests(diary.getId());
            model.addAttribute("friends", allRequests);
            return "manage/familyRequests";
        }
        return "manage/familyRequests";
    }


    @GetMapping("/friends_diary_view/{email}")
    public String friends_diary_view(Model model, @PathVariable String email) {


        Diary diary = diaryRepo.findByEmail(email).get(0);

       List<User> reqUser = userRepo.findByEmail(email);
        List<Post> myPosts = postRepo.findByUser(reqUser.get(0).getId());

        model.addAttribute("friendPosts",myPosts);
        model.addAttribute("friends", diary.getFriends());
        model.addAttribute("friendEmail",email);
        model.addAttribute("frndName",reqUser.get(0).getName()+"'s diary");
        return "user/mydiary_viewFriends_friends";
    }

    @GetMapping("/friends_diary_familyTree/{email}")
    public String friends_diary_familyTree(Model model, @PathVariable String email) {




        Optional<GoogleTree> ft = googleTreeRepo.findByEmail(email);
        List<Diary> diary = diaryRepo.findByEmail(email);
        User user = userRepo.findByEmail(email).get(0);

        model.addAttribute("name",user.getName());
        model.addAttribute("treeExists",ft.isPresent()?true:false);
        model.addAttribute("nottreeExists",!ft.isPresent()?true:false);
        model.addAttribute("friends", diary.get(0).getFamilyMembers());
        model.addAttribute("frndName",user.getName()+"'s diary");
        model.addAttribute("friendEmail",email);



        Optional<GoogleTree> myTree = googleTreeRepo.findByEmail(email);

        if(myTree.isPresent()) {
            List<List<Object>> chartData = new ArrayList<>();
            myTree.get().getData().forEach(x -> chartData.add(Arrays.asList(x.getName(), x.getParent(), x.getTooltip())));
            model.addAttribute("data", chartData);

        }
        return "user/mydiary_viewFriends_family";
    }


    @GetMapping("/manage/family/{email}")
    public String manageFamily(Model model, @PathVariable String email) {
        {


            Optional<GoogleTree> ft = googleTreeRepo.findByEmail(email);
            List<Diary> diary = diaryRepo.findByEmail(email);
            User user = userRepo.findByEmail(email).get(0);

            model.addAttribute("name", user.getName());
            model.addAttribute("treeExists", ft.isPresent() ? true : false);
            model.addAttribute("nottreeExists", !ft.isPresent() ? true : false);
            model.addAttribute("friends", diary.get(0).getFamilyMembers());
            model.addAttribute("frndName", user.getName() + "'s diary");
            model.addAttribute("friendEmail", email);


            Optional<GoogleTree> myTree = googleTreeRepo.findByEmail(email);

            if (myTree.isPresent()) {
                List<List<Object>> chartData = new ArrayList<>();
                myTree.get().getData().forEach(x -> chartData.add(Arrays.asList(x.getName(), x.getParent(), x.getTooltip())));
                model.addAttribute("data", chartData);

            }
            return "manage/manage_googleTree";
        }
    }

    @GetMapping("/manage/ft/add/{name}/{addas}/{curName}/{otherEmail}")
    public String manageAddPerson(@PathVariable String otherEmail,@PathVariable String name, @PathVariable String addas, Model model,@PathVariable String curName) {
        System.out.println(otherEmail);
        String email = otherEmail;
        Optional<GoogleTree> ft = googleTreeRepo.findByEmail(email);
        model.addAttribute("treeExists",ft.isPresent()?true:false);
        model.addAttribute("nottreeExists",!ft.isPresent()?true:false);

        model.addAttribute("friendEmail", email);

        if (addas.equalsIgnoreCase("CHILD")){
            ft.get().getData().add(new Person(name,curName , ""));
            googleTreeRepo.save(ft.get());
        }
        if (addas.equalsIgnoreCase("PARENT")){

            String newUser = name;
            String currentNode = curName;
            String currentParent = ft.get().getData().stream().filter(x->x.getName().equals(currentNode)).toList().get(0).getParent();

            Person newPerson = new Person(newUser, currentParent, "");

            ft.get().getData().stream().filter(x->x.getName().equals(currentNode)).toList().get(0).setParent(newUser);
            ft.get().getData().add(newPerson);
            googleTreeRepo.save(ft.get());
        }
        return "manage/manage_googleTree";
    }




}
