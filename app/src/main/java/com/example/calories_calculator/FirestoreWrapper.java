package com.example.calories_calculator;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreWrapper {
    private final FirebaseFirestore db;
    String userMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    public FirestoreWrapper(){
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<DocumentSnapshot> getDocument(String pathToDoc){
        DocumentReference docRef = this.db.document(pathToDoc);
        return docRef.get();
    }

    public DocumentReference getDocumentRef(String pathToDoc){
        return this.db.document(pathToDoc);
    }

    public CollectionReference getCollectionRef(String pathToCollection){
        return this.db.collection(pathToCollection);
    }

    public Task<Void> setDocument(String path, Map<String, Object> data){
        return this.db.document(path).set(data);
    }

    public static class UserMainScreenWrapper extends FirestoreWrapper{
        UserMainScreenWrapper thisScreenWrapper = this;
        public UserMainScreenWrapper(){
            super();
        }

        public void getUserMenus(UserMainScreen screen){
            this.getCollectionRef("users/"+userMail+"/menus")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get menus");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.userMenus.put(document.getId(), document.getData());
                                }
                                screen.mainFunction();
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        public void removeMenu(String menuName){
            this.getCollectionRef("users/" + userMail + "/menus/" + menuName + "/meals")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get meals to delete");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });

            this.getDocumentRef("users/" + userMail + "/menus/"+menuName)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("mainActivity", "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("mainActivity", "Error deleting document", e);
                        }
                    });
        }

        void addNewMenu(UserMainScreen screen,String menuName){
            Map<String, Object> menu = new HashMap<>();
            menu.put("totalCals", 0);
            this.setDocument("users/" + userMail + "/menus/"+menuName,menu)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            thisScreenWrapper.getUserMenus(screen);
                        }
                    });
        }

        public void getUserName(UserMainScreen screen){
            this.getDocument("users/"+userMail)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    screen.userName = (String) document.getData().get("name");
                                    String adminRequest = (String) document.getData().get("message");
                                    if (adminRequest != null){
                                        screen.AdminRequest(adminRequest);
                                    }
                                    thisScreenWrapper.getUserMenus(screen);
                                }
                            }
                        }
                    });
        }

    }

    public static class UserSearchWrapper extends FirestoreWrapper{
        UserSearchWrapper thisWrapper = this;
        UserSearch screen;
        public UserSearchWrapper(UserSearch screen){
            super();
            this.screen = screen;
        }

        void getUserExistingMeals(String menuName){
            this.getCollectionRef("users/" + userMail + "/menus/" + menuName + "/meals")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get menus");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.userExistingMeals.get(menuName).add(document.getId());
                                }
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        void getUserExistingMenus(){
            this.getCollectionRef("users/" + userMail + "/menus")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get menus");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.userExistingMeals.put(document.getId(), new ArrayList<String>());
                                    thisWrapper.getUserExistingMeals(document.getId());
                                }
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        void addItemToMeal(DocumentReference docRef, String itemName, int quantity){
            DocumentReference itemRef = this.getDocumentRef("foods/"+itemName);
            Map<String, Object> newItem = new HashMap<>();
            newItem.put("foodRef", itemRef);
            newItem.put("quantity", quantity);
            docRef.update("foods", FieldValue.arrayUnion(newItem));
            Long totalAddCals = ((Long) screen.products.get(itemName).get("calories")) * quantity;
            docRef.update("totalCals", FieldValue.increment(totalAddCals));
            docRef.getParent().getParent().update("totalCals", FieldValue.increment(totalAddCals));
            Toast.makeText(screen, quantity + " " +itemName +" added to your meal", Toast.LENGTH_SHORT).show();
        }

        void validateItemOnMeal(String menuName, String mealName, String itemName, int quantity){
            DocumentReference itemRef = this.getDocumentRef("foods/"+itemName);
            DocumentReference docRef = this.getDocumentRef("users/"+userMail+"/menus/"+menuName+"/meals/"+mealName);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            ArrayList<Map<String, Object>> foodList = (ArrayList<Map<String, Object>>) document.getData().get("foods");
                            for (int i =0; i< foodList.size();i++){
                                if (itemRef.equals(foodList.get(i).get("foodRef"))){
                                    Toast.makeText(screen, "you already have "+ itemName + " in this meal", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            thisWrapper.addItemToMeal(docRef, itemName, quantity);
                        }
                    }
                }
            });
        }

        void getProductsRef(String text){
            DocumentReference docRef = this.getDocumentRef("food types/"+text);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            ArrayList<DocumentReference> foodList = (ArrayList<DocumentReference>) document.getData().get("foods");
                            for (int i =0; i< foodList.size();i++){
                                String name = foodList.get(i).getId();
                                thisWrapper.getProducts(name, foodList.size());
                            }
                        }
                    }
                }
            });
        }

        void getProducts(String item, int totalProducts){
            this.getDocument("foods/"+item)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    screen.products.put(item,(HashMap<String, Object>)document.getData());
                                    if (screen.products.size() == totalProducts){
                                        screen.showProducts();
                                    }
                                }
                            }
                        }
                    });
        }
    }

    public static class UserSuggestionsWrapper extends FirestoreWrapper{
        UserSuggestionsWrapper thisWrapper = this;
        UserSuggestions screen;
        public UserSuggestionsWrapper(UserSuggestions screen){
            super();
            this.screen = screen;
        }

        void getAdminSuggestionMenus(){
            screen.adminRef.collection("menus")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.suggestionMenus.put(document.getId(), document.getData());
                                }
                                screen.addMeals();
                            }
                        }
                    });
        }

        void getGeneralSuggestionMenus(){
            this.getCollectionRef("users/" + "admin@gmail.com/" + "menus")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.suggestionMenus.put(document.getId(), document.getData());
                                }
                                thisWrapper.getUserData();
                            }
                        }
                    });
        }

        void getUserData(){
            String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            DocumentReference docRef = this.getDocumentRef("users/"+mail);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            screen.userName = (String) document.getData().get("name");
                            screen.adminRef = (DocumentReference) document.getData().get("adminRef");
                            if (screen.adminRef != null){
                                thisWrapper.getAdminSuggestionMenus();
                            }
                            else{
                                screen.addMeals();
                            }
                        }
                    }
                }
            });
        }
    }

    public static class UserSuggestionsMenuWrapper extends FirestoreWrapper{
        UserSuggestionsMenuWrapper thisWrapper = this;
        UserSuggestionsMenu screen;
        public UserSuggestionsMenuWrapper(UserSuggestionsMenu screen){
            super();
            this.screen = screen;
        }

        void getAdminSuggestionMeals(){
            screen.adminRef.collection("menus/" + screen.suggestionMenuName + "/meals")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get menus");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.suggestionMeals.put(document.getId(), document.getData());
                                }
                                screen.addMeals();
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        void getGeneralSuggestionMeals(){
            this.getCollectionRef("users/" + "admin@gmail.com/" + "menus/" + screen.suggestionMenuName + "/meals")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get menus");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.suggestionMeals.put(document.getId(), document.getData());
                                }
                                thisWrapper.getUserData();
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        void updateMenuTotalCals(String menuName, Long totalMealCals){
            DocumentReference menuDocRef = this.getDocumentRef("users/" + userMail +"/menus/"+menuName);
            menuDocRef.update("totalCals", FieldValue.increment(totalMealCals));
        }

        void addToUserMenu(String menuName, String mealName, Map<String, Object> mealData){
            Long totalAddCals = (Long) mealData.get("totalCals");
            this.setDocument("users/" + userMail + "/menus/" + menuName + "/meals/"+mealName, mealData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("mainActivity", "user successfully written to DB!");
                            thisWrapper.updateMenuTotalCals(menuName, totalAddCals);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("mainActivity", "Error writing user document", e);
                        }
                    });
        }

        void getUserExistingMenus(){
            this.getCollectionRef("users/" + userMail + "/menus")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("mainActivity", "success get menus");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.userExistingMenus.add(document.getId());
                                }
                            } else {
                                Log.d("mainActivity", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

        void getUserData(){
            DocumentReference docRef = this.getDocumentRef("users/"+userMail);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            screen.userName = (String) document.getData().get("name");
                            screen.adminRef = (DocumentReference) document.getData().get("adminRef");
                            if (screen.adminRef != null){
                                thisWrapper.getAdminSuggestionMeals();
                            }
                            else{
                                screen.addMeals();
                            }
                        }
                    }
                }
            });
        }
    }

    public static class UserAddProductToMealWrapper extends FirestoreWrapper {
        UserAddProductToMealWrapper thisWrapper = this;
        UserAddProductToMeal screen;

        public UserAddProductToMealWrapper(UserAddProductToMeal screen) {
            super();
            this.screen = screen;
        }

        void removeProduct(String productName,Long oldAmount, Long calories){
            DocumentReference docRef = this.getDocumentRef("users/"+screen.mail+"/menus/"+screen.menuName+"/meals/"+screen.mealName);
            DocumentReference itemRef = this.getDocumentRef("foods/"+productName);
            Map<String, Object> newItem = new HashMap<>();
            newItem.put("foodRef", itemRef);
            newItem.put("quantity",oldAmount.intValue());
            docRef.update("foods", FieldValue.arrayRemove(newItem));
            Long totalAddCals = (-1)*calories;
            docRef.update("totalCals", FieldValue.increment(totalAddCals));
            docRef.getParent().getParent().update("totalCals", FieldValue.increment(totalAddCals));
            Toast.makeText(screen, oldAmount + " " +productName +" removed from your meal", Toast.LENGTH_SHORT).show();
            screen.userProducts.clear();
            thisWrapper.getUserProducts();
        }

        void addAmountProduct(String productName,Long newAmount,Long oldAmount, Long calories){
            DocumentReference docRef = this.getDocumentRef("users/"+screen.mail+"/menus/"+screen.menuName+"/meals/"+screen.mealName);
            DocumentReference itemRef = this.getDocumentRef("foods/"+productName);
            Map<String, Object> newItem = new HashMap<>();
            newItem.put("foodRef", itemRef);
            newItem.put("quantity",oldAmount.intValue());
            docRef.update("foods", FieldValue.arrayRemove(newItem));
            newItem.clear();
            newItem.put("foodRef", itemRef);
            newItem.put("quantity", newAmount.intValue());
            docRef.update("foods", FieldValue.arrayUnion(newItem));
            Long totalAddCals = (newAmount-oldAmount)*calories;
            docRef.update("totalCals", FieldValue.increment(totalAddCals));
            docRef.getParent().getParent().update("totalCals", FieldValue.increment(totalAddCals));
            Toast.makeText(screen, productName +" amount updated to " + newAmount + " in your meal", Toast.LENGTH_SHORT).show();
            screen.userProducts.clear();
            thisWrapper.getUserProducts();
        }

        void getProductsCalories(DocumentReference foods, Map<String, Object> mealItem, int totalItems){
            foods.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String,Object> product = document.getData();
                            mealItem.put("calories",product.get("calories"));
                            screen.userProducts.add(mealItem);
                            if (screen.userProducts.size() == totalItems){
                                screen.addMealsProducts();
                            }
                            Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d("mainActivity", "No such document");
                        }
                    } else {
                        Log.d("mainActivity", "get failed with ", task.getException());
                    }
                }
            });
        }

        void getUserProducts(){
            this.getDocument("users/" + screen.mail + "/menus/" + screen.menuName + "/meals/"+screen.mealName)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                                    ArrayList<Map<String, Object>> mealProducts = (ArrayList<Map<String, Object>>) document.getData().get("foods");
                                    for (int i = 0; i < mealProducts.size(); i++) {
                                        DocumentReference food = (DocumentReference) mealProducts.get(i).get("foodRef");
                                        thisWrapper.getProductsCalories(food, mealProducts.get(i), mealProducts.size());
                                    }
                                }
                            }
                        }
                    });
        }
    }

    public static class MenuPageWrapper extends FirestoreWrapper {
        MenuPageWrapper thisWrapper = this;
        MenuPage screen;

        public MenuPageWrapper(MenuPage screen) {
            super();
            this.screen = screen;
        }

        void addNewMeal(String mealName){
            Map<String, Object> meal = new HashMap<>();
            ArrayList<Map<String, Object>> foods = new ArrayList<>();
            meal.put("totalCals", 0);
            meal.put("foods", foods);
            this.setDocument("users/" + userMail + "/menus/" + screen.menuName + "/meals/"+mealName, meal)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            thisWrapper.getUserMeals();
                        }
                    });
        }

        void removeMeal(String mealName){
            Long totalMealCals = ((Long) screen.userMeals.get("totalCals")) * -1;
            this.getDocumentRef("users/" + userMail + "/menus/" + screen.menuName + "/meals/"+mealName)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("mainActivity", "DocumentSnapshot successfully deleted!");
                            thisWrapper.updateMenuTotalCals(totalMealCals);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("mainActivity", "Error deleting document", e);
                        }
                    });
        }

        void updateMenuTotalCals(Long totalMealCals){
            DocumentReference menuDocRef = this.getDocumentRef("users/" +userMail+"/menus/"+screen.menuName);
            menuDocRef.update("totalCals", FieldValue.increment(totalMealCals));
        }

        void getUserMeals(){
            this.getCollectionRef("users/" + userMail + "/menus/" + screen.menuName + "/meals")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    screen.userMeals.put(document.getId(), document.getData());
                                }
                                screen.mainFunction();
                            }
                        }
                    });
        }
    }

    public static class LoginWrapper extends FirestoreWrapper {
        LoginWrapper thisWrapper = this;
        Login screen;

        public LoginWrapper(Login screen) {
            super();
            this.screen = screen;
        }

        void getUserData(String mail){
            this.getDocument("users/"+mail)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    screen.connect((Boolean) document.getData().get("isAdmin"));
                                }
                            }
                        }
                    });
        }
    }
}
