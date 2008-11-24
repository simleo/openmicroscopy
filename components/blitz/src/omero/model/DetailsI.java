/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */

package omero.model;

import ome.model.internal.Permissions;


// Generated by templates/java_objects.vm



public class DetailsI 
  extends Details 
  implements ome.model.ModelBased 

{ 

 
    public omero.model.ExperimenterI getOwner() {
        return (omero.model.ExperimenterI)this.owner ;
    }
    
    public void setOwner(omero.model.ExperimenterI owner) {
        this.owner =  owner ;
         
    }
 
    public omero.model.ExperimenterGroupI getGroup() {
        return (omero.model.ExperimenterGroupI)this.group ;
    }
    
    public void setGroup(omero.model.ExperimenterGroupI group) {
        this.group =  group ;
         
    }
 
    public omero.model.EventI getCreationEvent() {
        return (omero.model.EventI)this.creationEvent ;
    }
    
    public void setCreationEvent(omero.model.EventI creationEvent) {
        this.creationEvent =  creationEvent ;
         
    }
 
    public omero.model.EventI getUpdateEvent() {
        return (omero.model.EventI)this.updateEvent ;
    }
    
    public void setUpdateEvent(omero.model.EventI updateEvent) {
        this.updateEvent =  updateEvent ;
         
    }
 
    public omero.model.PermissionsI getPermissions() {
        return (omero.model.PermissionsI)this.permissions ;
    }
    
    public void setPermissions(omero.model.PermissionsI permissions) {
        this.permissions =  permissions ;
         
    }
 
    public omero.model.ExternalInfoI getExternalInfo() {
        return (omero.model.ExternalInfoI)this.externalInfo ;
    }
    
    public void setExternalInfo(omero.model.ExternalInfoI externalInfo) {
        this.externalInfo =  externalInfo ;
         
    }
 
    public void copyObject(ome.util.Filterable model, ome.util.ModelMapper _mapper) {
        omero.util.IceMapper mapper = (omero.util.IceMapper) _mapper;
        if (model instanceof ome.model.internal.Details){
            ome.model.internal.Details source = (ome.model.internal.Details) model;
            this.setOwner((omero.model.ExperimenterI) mapper.findTarget(source.getOwner()));
            this.setGroup((omero.model.ExperimenterGroupI) mapper.findTarget(source.getGroup()));
            this.setCreationEvent((omero.model.EventI) mapper.findTarget(source.getCreationEvent()));
            this.setUpdateEvent((omero.model.EventI) mapper.findTarget(source.getUpdateEvent()));
            this.setExternalInfo((omero.model.ExternalInfoI) mapper.findTarget(source.getExternalInfo()));
            ome.model.internal.Permissions sourceP = source.getPermissions();
            PermissionsI targetP = new PermissionsI();
            targetP.setPerm1((Long)ome.util.Utils.internalForm(sourceP));
            this.setPermissions(targetP);
	  } else {
             throw new IllegalArgumentException(
               "Details cannot copy from " + 
               (model==null ? "null" : model.getClass().getName()));
	  }
    }

     public ome.util.Filterable fillObject(ome.util.ReverseModelMapper _mapper) {
        omero.util.IceMapper mapper = (omero.util.IceMapper) _mapper;
        ome.model.internal.Details target = ome.model.internal.Details.create();
        mapper.store(this, target);
        target.putAt(ome.model.internal.Details.OWNER, (ome.model.meta.Experimenter) mapper.reverse( (ome.model.ModelBased) this.getOwner() ) );
        target.putAt(ome.model.internal.Details.GROUP, (ome.model.meta.ExperimenterGroup) mapper.reverse( (ome.model.ModelBased) this.getGroup() ) );
        target.putAt(ome.model.internal.Details.CREATIONEVENT, (ome.model.meta.Event) mapper.reverse( (ome.model.ModelBased) this.getCreationEvent() ) );
        target.putAt(ome.model.internal.Details.UPDATEEVENT, (ome.model.meta.Event) mapper.reverse( (ome.model.ModelBased) this.getUpdateEvent() ) );
        target.putAt(ome.model.internal.Details.EXTERNALINFO, (ome.model.meta.ExternalInfo) mapper.reverse( (ome.model.ModelBased) this.getExternalInfo() ) );
        PermissionsI sourceP = this.getPermissions();
        target.putAt(ome.model.internal.Details.PERMISSIONS, sourceP==null?new ome.model.internal.Permissions(Permissions.DEFAULT):ome.util.Utils.toPermissions(sourceP.getPerm1()));
        return target;
    }


    public void unload(Ice.Current c) {
      this.setOwner( null );
      this.setGroup( null );
      this.setCreationEvent( null );
      this.setUpdateEvent( null );
      this.setPermissions( null );
      this.setExternalInfo( null );
    }

}

