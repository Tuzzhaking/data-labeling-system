package Project.Problem;

import Project.Dataset;
import Project.Instance;
import Project.JsonIO.JsonFileWriter;
import Project.Labeling.RandomMechanism;
import Project.Labeling.RuleBasedMechanism;
import Project.Labeling.UserMechanism;
import Project.User;
import Project.UserInterface;

import java.util.ArrayList;
import java.util.Comparator;

public class SelectedProblem extends Problem {

    //Solution mechanism for this class
    public void runMechanism(ArrayList<User> users, Dataset dataset,ArrayList<Dataset>datasets) {
        // defining problem type , for now we just use randomLabeling
        // with this method , we randomly select the number of users which labels the instance , for example
        // for 1. instance , program selects random number of users , it can select 1 user or 3 user for 1 instance ,
        // for each instance , users labels the instances different.
        UserInterface.getUserInterface().run(users);
        ArrayList<User> selectedUsers = new ArrayList<>();
        if(UserInterface.getUserInterface().getAuthenticatedUser()==null){
            selectedUsers = selectUsers(users,selectedUsers,dataset);    // select users from dataset
        // sort the list
        selectedUsers.sort(Comparator.comparingLong(User::getUserID));
        }
        else
            selectedUsers.add(UserInterface.getUserInterface().getAuthenticatedUser());
   //     dataset.setAssignedUsers(selectedUsers);                // write users to dataset
        for(User currentUser : selectedUsers){
            if(currentUser.assignedDataset(dataset)==null)continue;

            ArrayList<Instance> instancesToLabel = new ArrayList<>() ;
            ArrayList<Instance> labeledInstances = new ArrayList<>() ;
            boolean control = false ;
            for(Instance currentInstance : dataset.getInstances()){
                for (Instance userInstances : currentUser.getInstances(dataset)){
                    if (userInstances.getId()==currentInstance.getId() &&
                            userInstances.getInstance().equals(currentInstance.getInstance())){
                        labeledInstances.add(currentInstance);
                        control = true ;
                        break;
                    }
                }
                if (!control)
                    instancesToLabel.add(currentInstance);
                control=false ;
            }

            if (instancesToLabel.size()==0){
                JsonFileWriter jsonfilewriter=new JsonFileWriter();
                jsonfilewriter.export(datasets, users, dataset);
            }

            for(Instance currentInstance : instancesToLabel){

               // y??zde 10 ihtimalle ??ncekilerden alacak
                double consistencyCheckRandom = (int)(Math.random()*100);
                double consistencyCheckProbability = currentUser.getConsistencyCheckProbability()*100;

                if (consistencyCheckRandom < consistencyCheckProbability && labeledInstances.size()!=0){
                    int previousSelectRandom = (int)(Math.random()*(labeledInstances.size()));
                    Instance copyInstance = new Instance(labeledInstances.get(previousSelectRandom).getId(),
                                                         labeledInstances.get(previousSelectRandom).getInstance());
                    if (currentUser.getUserType().equals("RandomBot")) super.labelingMechanism=new RandomMechanism();
                    else if(currentUser.getUserType().equals("Human")) super.labelingMechanism=new UserMechanism();
                    else if(currentUser.getUserType().equals("RuleBasedBot")) super.labelingMechanism=new RuleBasedMechanism();
                    super.labelingMechanism.labelingMechanism(currentUser,copyInstance,dataset.getLabels(),dataset,users,datasets);
                }

                // y??zde 60 s??radakini ekleyecek
                int nextCheckRandom = (int)(Math.random()*100);
                if (currentUser.getUserType().equals("Human") || nextCheckRandom < 60){
                    Instance copyInstance = new Instance(currentInstance.getId(),currentInstance.getInstance());
                    if (currentUser.getUserType().equals("RandomBot")) super.labelingMechanism=new RandomMechanism();
                    else if(currentUser.getUserType().equals("Human")) super.labelingMechanism=new UserMechanism();
                    else if(currentUser.getUserType().equals("RuleBasedBot")) super.labelingMechanism=new RuleBasedMechanism();
                    super.labelingMechanism.labelingMechanism(currentUser,copyInstance,dataset.getLabels(),dataset,users,datasets);
                }

            }

            labeledInstances.clear();
            instancesToLabel.clear();

        }

    }

    // in this method , we create com.User array which we select the number of user and select the user or users randomly.
    public ArrayList<User> selectUsers(ArrayList<User> users, ArrayList<User> selectedUsers,Dataset dataset){
        for (User user: users) {
                if(user.assignedDataset(dataset)!=null && !user.getUserType().equals("Human")){
                    selectedUsers.add(user);
                }
        }
        return selectedUsers ;

    }

}
